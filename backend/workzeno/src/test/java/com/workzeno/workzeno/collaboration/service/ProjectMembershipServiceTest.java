package com.workzeno.workzeno.collaboration.service;

import com.workzeno.workzeno.collaboration.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.domain.ProjectMembershipRole;
import com.workzeno.workzeno.collaboration.dto.AddProjectMemberRequest;
import com.workzeno.workzeno.collaboration.dto.ProjectMemberResponse;
import com.workzeno.workzeno.collaboration.dto.UpdateProjectMemberRoleRequest;
import com.workzeno.workzeno.collaboration.mapper.ProjectMembershipMapper;
import com.workzeno.workzeno.collaboration.repository.ProjectMembershipRepository;
import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.domain.ProjectStatus;
import com.workzeno.workzeno.projects.service.ProjectAccessService;
import com.workzeno.workzeno.shared.exception.ApiException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMembershipServiceTest {

    private final UUID projectId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID newUserId = UUID.randomUUID();

    @Mock
    private ProjectMembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private ProjectAuthorizationService authorizationService;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private ProjectMembershipMapper membershipMapper;

    @Mock
    private User owner;

    @Mock
    private User newUser;

    @Mock
    private Project project;

    @InjectMocks
    private ProjectMembershipService membershipService;


// ---------------------------------------------------------------
// addMember
// ---------------------------------------------------------------

    @Test
    void addMember_ownerCanAddMember() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.of(newUser));

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        newUserId,
                        ProjectMembershipRole.MEMBER
                );

        membershipService.addMember(projectId, request);

        verify(authorizationService)
                .requireOwnerOrAdmin(project, ownerId);

        verify(userRepository).findById(newUserId);
        verify(membershipRepository)
                .saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_ownerCanAddAdmin() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.of(newUser));

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        newUserId,
                        ProjectMembershipRole.ADMIN
                );

        membershipService.addMember(projectId, request);

        verify(authorizationService).requireOwner(project, ownerId);
        verify(membershipRepository)
                .saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_adminCanAddMember() {
        authenticateAndStubProjectOwner(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.of(newUser));

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        newUserId,
                        ProjectMembershipRole.MEMBER
                );

        membershipService.addMember(projectId, request);

        verify(authorizationService)
                .requireOwnerOrAdmin(project, adminId);

        verify(membershipRepository)
                .saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_adminCannotAddAdmin() {
        authenticateAs(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        doThrow(new ApiException(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Only the project owner can perform this action"
        )).when(authorizationService)
                .requireOwner(project, adminId);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        newUserId,
                        ProjectMembershipRole.ADMIN
                );

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.FORBIDDEN
        );

        verify(membershipRepository, never())
                .saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_rejectsDuplicateMembership() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(membershipRepository.existsByProjectIdAndUserId(
                projectId,
                newUserId
        )).thenReturn(true);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        newUserId,
                        ProjectMembershipRole.MEMBER
                );

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.CONFLICT
        );

        verify(membershipRepository, never())
                .saveAndFlush(any(ProjectMembership.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    void addMember_translatesDatabaseDuplicateConstraintViolation() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.of(newUser));

        DataIntegrityViolationException databaseException =
                new DataIntegrityViolationException(
                        "Duplicate membership",
                        new ConstraintViolationException(
                                "Duplicate membership",
                                new SQLException(),
                                "uq_project_memberships_project_user"
                        )
                );

        when(membershipRepository.saveAndFlush(
                any(ProjectMembership.class)
        )).thenThrow(databaseException);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        newUserId,
                        ProjectMembershipRole.MEMBER
                );

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.CONFLICT
        );
    }

    @Test
    void addMember_rejectsNonexistentUser() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.empty());

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        newUserId,
                        ProjectMembershipRole.MEMBER
                );

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.NOT_FOUND
        );

        verify(membershipRepository, never())
                .saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_rejectsOwnerAsTarget() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        ownerId,
                        ProjectMembershipRole.MEMBER
                );

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.BAD_REQUEST
        );

        verifyNoInteractions(userRepository);
        verify(membershipRepository, never())
                .saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_rejectsNullRole() {
        authenticateAs(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(newUserId, null);

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.BAD_REQUEST
        );

        verifyNoInteractions(authorizationService);
        verifyNoInteractions(userRepository);
        verify(membershipRepository, never())
                .saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_rejectsArchivedProject() {
        authenticateAs(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ARCHIVED);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(
                        newUserId,
                        ProjectMembershipRole.MEMBER
                );

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.CONFLICT
        );

        verifyNoInteractions(authorizationService);
        verifyNoInteractions(userRepository);
        verify(membershipRepository, never())
                .saveAndFlush(any(ProjectMembership.class));
    }


// ---------------------------------------------------------------
// getProjectMembers
// ---------------------------------------------------------------

    @Test
    void getProjectMembers_ownerCanViewMembers() {
        assertMemberListingAccessibleTo(ownerId);
    }

    @Test
    void getProjectMembers_adminCanViewMembers() {
        assertMemberListingAccessibleTo(adminId);
    }

    @Test
    void getProjectMembers_memberCanViewMembers() {
        assertMemberListingAccessibleTo(memberId);
    }

    @Test
    void getProjectMembers_nonMemberCannotViewMembers() {
        UUID nonMemberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(nonMemberId);

        when(projectAccessService.loadAccessibleProject(
                projectId,
                nonMemberId
        )).thenThrow(new ApiException(
                HttpStatus.NOT_FOUND,
                "Project not found",
                "Project does not exist or you do not have access to it"
        ));

        assertApiException(
                () -> membershipService.getProjectMembers(
                        projectId,
                        pageable
                ),
                HttpStatus.NOT_FOUND
        );

        verifyNoInteractions(membershipRepository);
        verifyNoInteractions(membershipMapper);
    }

    @Test
    void getProjectMembers_mapsMembershipsAndPreservesPagination() {
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 2);

        ProjectMembership firstMembership =
                mock(ProjectMembership.class);

        ProjectMembership secondMembership =
                mock(ProjectMembership.class);

        ProjectMemberResponse firstResponse =
                new ProjectMemberResponse(
                        firstUserId,
                        "first@example.com",
                        "MEMBER",
                        Instant.parse("2026-01-01T10:00:00Z")
                );

        ProjectMemberResponse secondResponse =
                new ProjectMemberResponse(
                        secondUserId,
                        "second@example.com",
                        "ADMIN",
                        Instant.parse("2026-01-02T10:00:00Z")
                );

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(ownerId);

        when(projectAccessService.loadAccessibleProject(
                projectId,
                ownerId
        )).thenReturn(project);

        when(membershipRepository
                .findAllByProjectIdOrderByJoinedAtDesc(
                        projectId,
                        pageable
                )).thenReturn(new PageImpl<>(
                List.of(firstMembership, secondMembership),
                pageable,
                5
        ));

        when(membershipMapper.toProjectMemberResponse(firstMembership))
                .thenReturn(firstResponse);

        when(membershipMapper.toProjectMemberResponse(secondMembership))
                .thenReturn(secondResponse);

        Page<ProjectMemberResponse> result =
                membershipService.getProjectMembers(
                        projectId,
                        pageable
                );

        assertThat(result.getContent())
                .containsExactly(firstResponse, secondResponse);

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(2);

        verify(membershipMapper)
                .toProjectMemberResponse(firstMembership);

        verify(membershipMapper)
                .toProjectMemberResponse(secondMembership);
    }


// ---------------------------------------------------------------
// changeMemberRole
// ---------------------------------------------------------------

    @Test
    void changeMemberRole_ownerCanPromoteMemberToAdmin() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership =
                mock(ProjectMembership.class);

        when(membershipRepository.findByProjectIdAndUserIdForUpdate(
                projectId,
                memberId
        )).thenReturn(Optional.of(membership));

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        ProjectMembershipRole.ADMIN
                );

        membershipService.changeMemberRole(
                projectId,
                memberId,
                request
        );

        verify(authorizationService).requireOwner(project, ownerId);

        verify(membershipRepository)
                .findByProjectIdAndUserIdForUpdate(projectId, memberId);

        verify(membership)
                .changeRole(ProjectMembershipRole.ADMIN);
    }

    @Test
    void changeMemberRole_ownerCanDemoteAdminToMember() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership =
                mock(ProjectMembership.class);

        when(membershipRepository.findByProjectIdAndUserIdForUpdate(
                projectId,
                adminId
        )).thenReturn(Optional.of(membership));

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        ProjectMembershipRole.MEMBER
                );

        membershipService.changeMemberRole(
                projectId,
                adminId,
                request
        );

        verify(membership)
                .changeRole(ProjectMembershipRole.MEMBER);
    }

    @Test
    void changeMemberRole_adminCannotChangeRole() {
        authenticateAs(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        doThrow(new ApiException(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Only the project owner can perform this action"
        )).when(authorizationService)
                .requireOwner(project, adminId);

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        ProjectMembershipRole.ADMIN
                );

        assertApiException(
                () -> membershipService.changeMemberRole(
                        projectId,
                        memberId,
                        request
                ),
                HttpStatus.FORBIDDEN
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserIdForUpdate(
                        projectId,
                        memberId
                );
    }

    @Test
    void changeMemberRole_memberCannotChangeRole() {
        authenticateAs(memberId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        doThrow(new ApiException(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Only the project owner can perform this action"
        )).when(authorizationService)
                .requireOwner(project, memberId);

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        ProjectMembershipRole.ADMIN
                );

        assertApiException(
                () -> membershipService.changeMemberRole(
                        projectId,
                        adminId,
                        request
                ),
                HttpStatus.FORBIDDEN
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserIdForUpdate(
                        projectId,
                        adminId
                );
    }

    @Test
    void changeMemberRole_ownerCannotChangeTheirOwnRole() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        ProjectMembershipRole.MEMBER
                );

        assertApiException(
                () -> membershipService.changeMemberRole(
                        projectId,
                        ownerId,
                        request
                ),
                HttpStatus.BAD_REQUEST
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserIdForUpdate(
                        projectId,
                        ownerId
                );
    }

    @Test
    void changeMemberRole_rejectsNullRole() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership =
                mock(ProjectMembership.class);

        when(membershipRepository.findByProjectIdAndUserIdForUpdate(
                projectId,
                memberId
        )).thenReturn(Optional.of(membership));

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(null);

        assertApiException(
                () -> membershipService.changeMemberRole(
                        projectId,
                        memberId,
                        request
                ),
                HttpStatus.BAD_REQUEST
        );

        verify(membership, never()).changeRole(any());
    }

    @Test
    void changeMemberRole_rejectsMissingMembership() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        when(membershipRepository.findByProjectIdAndUserIdForUpdate(
                projectId,
                memberId
        )).thenReturn(Optional.empty());

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(
                        ProjectMembershipRole.ADMIN
                );

        assertApiException(
                () -> membershipService.changeMemberRole(
                        projectId,
                        memberId,
                        request
                ),
                HttpStatus.NOT_FOUND
        );
    }


// ---------------------------------------------------------------
// removeMember
// ---------------------------------------------------------------

    @Test
    void removeMember_ownerCanRemoveMember() {
        assertOwnerCanRemoveRole(
                ProjectMembershipRole.MEMBER
        );
    }

    @Test
    void removeMember_ownerCanRemoveAdmin() {
        assertOwnerCanRemoveRole(
                ProjectMembershipRole.ADMIN
        );
    }

    @Test
    void removeMember_adminCanRemoveMember() {
        authenticateAndStubProjectOwner(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership =
                mock(ProjectMembership.class);

        when(membership.getRole())
                .thenReturn(ProjectMembershipRole.MEMBER);

        when(membershipRepository.findByProjectIdAndUserIdForUpdate(
                projectId,
                memberId
        )).thenReturn(Optional.of(membership));

        membershipService.removeMember(projectId, memberId);

        verify(authorizationService)
                .requireOwnerOrAdmin(project, adminId);

        verify(membershipRepository).delete(membership);
    }

    @Test
    void removeMember_adminCannotRemoveAdmin() {
        authenticateAndStubProjectOwner(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership =
                mock(ProjectMembership.class);

        when(membership.getRole())
                .thenReturn(ProjectMembershipRole.ADMIN);

        when(membershipRepository.findByProjectIdAndUserIdForUpdate(
                projectId,
                memberId
        )).thenReturn(Optional.of(membership));

        assertApiException(
                () -> membershipService.removeMember(
                        projectId,
                        memberId
                ),
                HttpStatus.FORBIDDEN
        );

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    void removeMember_ownerCannotRemoveThemselves() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        assertApiException(
                () -> membershipService.removeMember(
                        projectId,
                        ownerId
                ),
                HttpStatus.BAD_REQUEST
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserIdForUpdate(
                        projectId,
                        ownerId
                );

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    void removeMember_nonMemberCannotRemoveAnyone() {
        UUID nonMemberId = UUID.randomUUID();

        // NOTE: authorization throws before the owner-target check is
        // reached, so we use authenticateAs (no owner stubs needed).
        authenticateAs(nonMemberId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        doThrow(new ApiException(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to perform this action"
        )).when(authorizationService)
                .requireOwnerOrAdmin(project, nonMemberId);

        assertApiException(
                () -> membershipService.removeMember(
                        projectId,
                        memberId
                ),
                HttpStatus.FORBIDDEN
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserIdForUpdate(
                        projectId,
                        memberId
                );

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    void removeMember_rejectsMissingMembership() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        when(membershipRepository.findByProjectIdAndUserIdForUpdate(
                projectId,
                memberId
        )).thenReturn(Optional.empty());

        assertApiException(
                () -> membershipService.removeMember(
                        projectId,
                        memberId
                ),
                HttpStatus.NOT_FOUND
        );

        verify(membershipRepository, never()).delete(any());
    }


// ---------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------

    private void assertMemberListingAccessibleTo(UUID userId) {
        Pageable pageable = PageRequest.of(0, 20);

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(userId);

        when(projectAccessService.loadAccessibleProject(
                projectId,
                userId
        )).thenReturn(project);

        when(membershipRepository
                .findAllByProjectIdOrderByJoinedAtDesc(
                        projectId,
                        pageable
                )).thenReturn(Page.empty(pageable));

        Page<ProjectMemberResponse> result =
                membershipService.getProjectMembers(
                        projectId,
                        pageable
                );

        assertThat(result).isEmpty();

        verify(projectAccessService)
                .loadAccessibleProject(projectId, userId);

        verify(membershipRepository)
                .findAllByProjectIdOrderByJoinedAtDesc(
                        projectId,
                        pageable
                );
    }

    private void assertOwnerCanRemoveRole(ProjectMembershipRole role) {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership =
                mock(ProjectMembership.class);

        when(membership.getRole()).thenReturn(role);

        when(membershipRepository.findByProjectIdAndUserIdForUpdate(
                projectId,
                memberId
        )).thenReturn(Optional.of(membership));

        membershipService.removeMember(projectId, memberId);

        verify(authorizationService)
                .requireOwnerOrAdmin(project, ownerId);

        verify(membershipRepository).delete(membership);
    }

    private void authenticateAs(UUID userId) {
        when(currentUserProvider.getCurrentUserId())
                .thenReturn(userId);

        when(projectAccessService.loadAccessibleProject(
                projectId,
                userId
        )).thenReturn(project);
    }

    /**
     * Stubs project ownership checks in addition to authentication.
     *
     * <p>The owner stubs are intentionally {@code lenient()}: this helper
     * is shared by tests that sometimes bail out (via authorization
     * failure, validation error, etc.) before the service reaches the
     * ownership check. Making them lenient keeps the shared setup clean
     * without weakening the strictness of the actual per-test stubs.</p>
     */
    private void authenticateAndStubProjectOwner(UUID userId) {
        authenticateAs(userId);

        lenient().when(project.getOwner()).thenReturn(owner);
        lenient().when(owner.getId()).thenReturn(ownerId);
    }

    private void assertApiException(
            Runnable action,
            HttpStatus expectedStatus
    ) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(
                        ApiException.class,
                        exception -> assertThat(exception.getStatus())
                                .isEqualTo(expectedStatus)
                );
    }
}