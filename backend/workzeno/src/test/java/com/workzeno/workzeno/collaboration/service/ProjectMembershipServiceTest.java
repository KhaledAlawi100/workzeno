package com.workzeno.workzeno.collaboration.service;

import com.workzeno.workzeno.collaboration.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.domain.ProjectMembershipRole;
import com.workzeno.workzeno.collaboration.dto.AddProjectMemberRequest;
import com.workzeno.workzeno.collaboration.dto.UpdateProjectMemberRoleRequest;
import com.workzeno.workzeno.collaboration.repository.ProjectMembershipRepository;
import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.domain.ProjectStatus;
import com.workzeno.workzeno.projects.service.ProjectAccessService;
import com.workzeno.workzeno.shared.exception.ApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMembershipServiceTest {

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

    @InjectMocks
    private ProjectMembershipService membershipService;

    private UUID projectId;
    private UUID ownerId;
    private UUID adminId;
    private UUID memberId;
    private UUID newUserId;

    private User owner;
    private User newUser;
    private Project project;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        newUserId = UUID.randomUUID();

        owner = mock(User.class);
        newUser = mock(User.class);
        project = mock(Project.class);
    }

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
                new AddProjectMemberRequest(newUserId, ProjectMembershipRole.MEMBER);

        membershipService.addMember(projectId, request);

        verify(authorizationService).requireOwnerOrAdmin(project, ownerId);
        verify(membershipRepository).saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_ownerCanAddAdmin() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.of(newUser));

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(newUserId, ProjectMembershipRole.ADMIN);

        membershipService.addMember(projectId, request);

        verify(authorizationService).requireOwner(project, ownerId);
        verify(membershipRepository).saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_adminCanAddMember() {
        authenticateAndStubProjectOwner(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.of(newUser));

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(newUserId, ProjectMembershipRole.MEMBER);

        membershipService.addMember(projectId, request);

        verify(authorizationService).requireOwnerOrAdmin(project, adminId);
        verify(membershipRepository).saveAndFlush(any(ProjectMembership.class));
    }

    @Test
    void addMember_adminCannotAddAdmin() {
        // Authorization throws before ensureOwnerIsNotTarget -> no owner stub
        authenticateAs(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        doThrow(new ApiException(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Only the project owner can perform this action"
        )).when(authorizationService).requireOwner(project, adminId);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(newUserId, ProjectMembershipRole.ADMIN);

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.FORBIDDEN
        );

        verify(membershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void addMember_rejectsDuplicateMembership() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(membershipRepository.existsByProjectIdAndUserId(projectId, newUserId))
                .thenReturn(true);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(newUserId, ProjectMembershipRole.MEMBER);

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.CONFLICT
        );

        verify(membershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void addMember_rejectsNonexistentUser() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.empty());

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(newUserId, ProjectMembershipRole.MEMBER);

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.NOT_FOUND
        );

        verify(membershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void addMember_rejectsArchivedProject() {
        // Status check throws before ensureOwnerIsNotTarget -> no owner stub
        authenticateAs(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ARCHIVED);

        AddProjectMemberRequest request =
                new AddProjectMemberRequest(newUserId, ProjectMembershipRole.MEMBER);

        assertApiException(
                () -> membershipService.addMember(projectId, request),
                HttpStatus.CONFLICT
        );

        verifyNoInteractions(authorizationService);
        verify(membershipRepository, never()).saveAndFlush(any());
    }

    // ---------------------------------------------------------------
    // changeMemberRole
    // ---------------------------------------------------------------

    @Test
    void changeMemberRole_ownerCanChangeMemberRole() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership = mock(ProjectMembership.class);

        when(membershipRepository.findByProjectIdAndUserId(projectId, memberId))
                .thenReturn(Optional.of(membership));

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(ProjectMembershipRole.ADMIN);

        membershipService.changeMemberRole(projectId, memberId, request);

        verify(authorizationService).requireOwner(project, ownerId);
        verify(membership).changeRole(ProjectMembershipRole.ADMIN);
    }

    @Test
    void changeMemberRole_adminCannotChangeRole() {
        // Authorization throws before ensureOwnerIsNotTarget -> no owner stub
        authenticateAs(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        doThrow(new ApiException(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Only the project owner can perform this action"
        )).when(authorizationService).requireOwner(project, adminId);

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(ProjectMembershipRole.ADMIN);

        assertApiException(
                () -> membershipService.changeMemberRole(projectId, memberId, request),
                HttpStatus.FORBIDDEN
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserId(projectId, memberId);
    }

    @Test
    void changeMemberRole_ownerCannotChangeTheirOwnRole() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        UpdateProjectMemberRoleRequest request =
                new UpdateProjectMemberRoleRequest(ProjectMembershipRole.MEMBER);

        assertApiException(
                () -> membershipService.changeMemberRole(projectId, ownerId, request),
                HttpStatus.BAD_REQUEST
        );

        verify(membershipRepository, never())
                .findByProjectIdAndUserId(projectId, ownerId);
    }

    // ---------------------------------------------------------------
    // removeMember
    // ---------------------------------------------------------------

    @Test
    void removeMember_ownerCanRemoveMember() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership = mock(ProjectMembership.class);

        when(membership.getRole()).thenReturn(ProjectMembershipRole.MEMBER);
        when(membershipRepository.findByProjectIdAndUserId(projectId, memberId))
                .thenReturn(Optional.of(membership));

        membershipService.removeMember(projectId, memberId);

        verify(authorizationService).requireOwnerOrAdmin(project, ownerId);
        verify(membershipRepository).delete(membership);
    }

    @Test
    void removeMember_adminCanRemoveMember() {
        authenticateAndStubProjectOwner(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership = mock(ProjectMembership.class);

        when(membership.getRole()).thenReturn(ProjectMembershipRole.MEMBER);
        when(membershipRepository.findByProjectIdAndUserId(projectId, memberId))
                .thenReturn(Optional.of(membership));

        membershipService.removeMember(projectId, memberId);

        verify(authorizationService).requireOwnerOrAdmin(project, adminId);
        verify(membershipRepository).delete(membership);
    }

    @Test
    void removeMember_adminCannotRemoveAnotherAdmin() {
        authenticateAndStubProjectOwner(adminId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        ProjectMembership membership = mock(ProjectMembership.class);

        when(membership.getRole()).thenReturn(ProjectMembershipRole.ADMIN);
        when(membershipRepository.findByProjectIdAndUserId(projectId, memberId))
                .thenReturn(Optional.of(membership));

        assertApiException(
                () -> membershipService.removeMember(projectId, memberId),
                HttpStatus.FORBIDDEN
        );

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    void removeMember_ownerCannotRemoveThemselves() {
        authenticateAndStubProjectOwner(ownerId);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        assertApiException(
                () -> membershipService.removeMember(projectId, ownerId),
                HttpStatus.BAD_REQUEST
        );

        verify(membershipRepository, never()).delete(any());
    }

    // ---------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------

    /**
     * Authenticates the calling user and stubs {@link ProjectAccessService}
     * to return the shared {@code project} mock. Does <em>not</em> stub
     * {@code project.getOwner()} — use this only for tests whose flow aborts
     * before {@code ensureOwnerIsNotTarget(...)} runs (authorization or
     * status failures). Otherwise use {@link #authenticateAndStubProjectOwner}.
     */
    private void authenticateAs(UUID userId) {
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(projectAccessService.loadAccessibleProject(projectId, userId))
                .thenReturn(project);
    }

    /**
     * Like {@link #authenticateAs(UUID)}, but additionally stubs the project
     * to have an owner whose id is {@code ownerId}. Needed by every test that
     * reaches {@code ensureOwnerIsNotTarget(...)}, which dereferences
     * {@code project.getOwner().getId()}. Strict stubbing requires this to be
     * absent from tests that never reach that line.
     */
    private void authenticateAndStubProjectOwner(UUID userId) {
        authenticateAs(userId);
        when(project.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(ownerId);
    }

    private void assertApiException(
            Runnable action,
            HttpStatus expectedStatus
    ) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus())
                                .isEqualTo(expectedStatus)
                );
    }
}