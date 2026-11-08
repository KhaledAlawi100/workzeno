
package com.workzeno.workzeno.collaboration.service;

import com.workzeno.workzeno.collaboration.domain.ProjectInvitation;
import com.workzeno.workzeno.collaboration.domain.ProjectInvitationStatus;
import com.workzeno.workzeno.collaboration.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.domain.ProjectMembershipRole;
import com.workzeno.workzeno.collaboration.dto.invitation.AcceptInvitationResponse;
import com.workzeno.workzeno.collaboration.dto.invitation.CreateInvitationRequest;
import com.workzeno.workzeno.collaboration.dto.invitation.CreateInvitationResponse;
import com.workzeno.workzeno.collaboration.dto.invitation.MyInvitationResponse;
import com.workzeno.workzeno.collaboration.mapper.ProjectInvitationMapper;
import com.workzeno.workzeno.collaboration.repository.ProjectInvitationRepository;
import com.workzeno.workzeno.collaboration.repository.ProjectMembershipRepository;
import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.domain.ProjectStatus;
import com.workzeno.workzeno.projects.service.ProjectAccessService;
import com.workzeno.workzeno.shared.exception.ApiException;
import com.workzeno.workzeno.shared.security.TokenGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectInvitationServiceTest {

    private static final String RAW_TOKEN = "generated-raw-token";

    private static final String TOKEN_HASH =
            "0123456789abcdef0123456789abcdef"
                    + "0123456789abcdef0123456789abcdef";

    private static final Duration INVITATION_TTL =
            Duration.ofDays(7);

    @Mock
    private ProjectInvitationRepository invitationRepository;

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
    private TokenGenerator tokenGenerator;

    @Mock
    private ProjectInvitationMapper invitationMapper;

    @InjectMocks
    private ProjectInvitationService invitationService;


    // =========================================================
    // CREATE INVITATION
    // =========================================================

    @Test
    void createInvitation_ownerCanInviteMember() {

        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();

        User owner = mock(User.class);
        User invitee = mock(User.class);
        Project project = mock(Project.class);

        setInvitationTtl();

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(ownerId);

        when(projectAccessService.loadAccessibleProject(projectId, ownerId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(project.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(ownerId);
        when(invitee.getId()).thenReturn(inviteeId);

        when(userRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.of(invitee));

        when(membershipRepository.existsByProjectIdAndUserId(
                projectId, inviteeId
        )).thenReturn(false);

        when(invitationRepository.existsByProject_IdAndInviteeEmailAndStatus(
                projectId,
                "newuser@example.com",
                ProjectInvitationStatus.PENDING
        )).thenReturn(false);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(tokenGenerator.generateToken()).thenReturn(RAW_TOKEN);
        when(tokenGenerator.hashToken(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        when(invitationRepository.saveAndFlush(any(ProjectInvitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubCreateInvitationResponse();

        CreateInvitationRequest request = new CreateInvitationRequest(
                "newuser@example.com",
                ProjectMembershipRole.MEMBER
        );

        CreateInvitationResponse result =
                invitationService.createInvitation(projectId, request);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(RAW_TOKEN);
        assertThat(result.inviteeEmail()).isEqualTo("newuser@example.com");
        assertThat(result.role()).isEqualTo(ProjectMembershipRole.MEMBER);

        verify(authorizationService)
                .requireOwnerOrAdmin(project, ownerId);

        verify(authorizationService, never())
                .requireOwner(any(), any());

        verifySuccessfulCreation(projectId, inviteeId,
                "newuser@example.com", ProjectMembershipRole.MEMBER);
    }


    @Test
    void createInvitation_ownerCanInviteAdmin() {

        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();

        User owner = mock(User.class);
        User invitee = mock(User.class);
        Project project = mock(Project.class);

        setInvitationTtl();

        when(currentUserProvider.getCurrentUserId()).thenReturn(ownerId);
        when(projectAccessService.loadAccessibleProject(projectId, ownerId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(project.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(ownerId);
        when(invitee.getId()).thenReturn(inviteeId);

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(invitee));

        when(membershipRepository.existsByProjectIdAndUserId(
                projectId, inviteeId
        )).thenReturn(false);

        when(invitationRepository.existsByProject_IdAndInviteeEmailAndStatus(
                projectId,
                "admin@example.com",
                ProjectInvitationStatus.PENDING
        )).thenReturn(false);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(tokenGenerator.generateToken()).thenReturn(RAW_TOKEN);
        when(tokenGenerator.hashToken(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        when(invitationRepository.saveAndFlush(any(ProjectInvitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubCreateInvitationResponse();

        CreateInvitationRequest request = new CreateInvitationRequest(
                "admin@example.com",
                ProjectMembershipRole.ADMIN
        );

        CreateInvitationResponse result =
                invitationService.createInvitation(projectId, request);

        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(ProjectMembershipRole.ADMIN);

        verify(authorizationService).requireOwner(project, ownerId);

        verify(authorizationService, never())
                .requireOwnerOrAdmin(any(), any());

        verifySuccessfulCreation(projectId, inviteeId,
                "admin@example.com", ProjectMembershipRole.ADMIN);
    }


    @Test
    void createInvitation_adminCanInviteMember() {

        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();

        User owner = mock(User.class);
        User admin = mock(User.class);
        User invitee = mock(User.class);
        Project project = mock(Project.class);

        setInvitationTtl();

        when(currentUserProvider.getCurrentUserId()).thenReturn(adminId);
        when(projectAccessService.loadAccessibleProject(projectId, adminId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(project.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(ownerId);
        when(invitee.getId()).thenReturn(inviteeId);

        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(invitee));

        when(membershipRepository.existsByProjectIdAndUserId(
                projectId, inviteeId
        )).thenReturn(false);

        when(invitationRepository.existsByProject_IdAndInviteeEmailAndStatus(
                projectId,
                "member@example.com",
                ProjectInvitationStatus.PENDING
        )).thenReturn(false);

        when(userRepository.findById(adminId))
                .thenReturn(Optional.of(admin));

        when(tokenGenerator.generateToken()).thenReturn(RAW_TOKEN);
        when(tokenGenerator.hashToken(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        when(invitationRepository.saveAndFlush(any(ProjectInvitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubCreateInvitationResponse();

        CreateInvitationRequest request = new CreateInvitationRequest(
                "member@example.com",
                ProjectMembershipRole.MEMBER
        );

        CreateInvitationResponse result =
                invitationService.createInvitation(projectId, request);

        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(ProjectMembershipRole.MEMBER);

        verify(authorizationService)
                .requireOwnerOrAdmin(project, adminId);

        verify(authorizationService, never())
                .requireOwner(any(), any());

        verifySuccessfulCreation(projectId, inviteeId,
                "member@example.com", ProjectMembershipRole.MEMBER);
    }


    @Test
    void createInvitation_adminCannotInviteAdmin() {

        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Project project = mock(Project.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(adminId);
        when(projectAccessService.loadAccessibleProject(projectId, adminId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        doThrow(new ApiException(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Only the project owner can perform this action"
        )).when(authorizationService).requireOwner(project, adminId);

        CreateInvitationRequest request = new CreateInvitationRequest(
                "admin@example.com",
                ProjectMembershipRole.ADMIN
        );

        assertApiException(
                () -> invitationService.createInvitation(projectId, request),
                HttpStatus.FORBIDDEN
        );

        verify(authorizationService).requireOwner(project, adminId);
        verifyNoInteractions(tokenGenerator);
        verifyNoInteractions(userRepository);
    }


    @Test
    void createInvitation_ordinaryMemberCannotInvite() {

        UUID projectId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Project project = mock(Project.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(memberId);
        when(projectAccessService.loadAccessibleProject(projectId, memberId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        doThrow(new ApiException(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Only project owners and admins can perform this action"
        )).when(authorizationService)
                .requireOwnerOrAdmin(project, memberId);

        CreateInvitationRequest request = new CreateInvitationRequest(
                "newuser@example.com",
                ProjectMembershipRole.MEMBER
        );

        assertApiException(
                () -> invitationService.createInvitation(projectId, request),
                HttpStatus.FORBIDDEN
        );

        verify(authorizationService)
                .requireOwnerOrAdmin(project, memberId);

        verifyNoInteractions(tokenGenerator);
        verifyNoInteractions(userRepository);
    }


    @Test
    void createInvitation_rejectsArchivedProject() {

        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Project project = mock(Project.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(ownerId);
        when(projectAccessService.loadAccessibleProject(projectId, ownerId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ARCHIVED);

        CreateInvitationRequest request = new CreateInvitationRequest(
                "newuser@example.com",
                ProjectMembershipRole.MEMBER
        );

        assertApiException(
                () -> invitationService.createInvitation(projectId, request),
                HttpStatus.CONFLICT
        );

        verifyNoInteractions(authorizationService);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(tokenGenerator);
    }


    @Test
    void createInvitation_rejectsExistingProjectMember() {

        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();

        User owner = mock(User.class);
        User invitee = mock(User.class);
        Project project = mock(Project.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(ownerId);
        when(projectAccessService.loadAccessibleProject(projectId, ownerId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(project.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(ownerId);
        when(invitee.getId()).thenReturn(inviteeId);

        when(userRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.of(invitee));

        when(membershipRepository.existsByProjectIdAndUserId(
                projectId, inviteeId
        )).thenReturn(true);

        CreateInvitationRequest request = new CreateInvitationRequest(
                "newuser@example.com",
                ProjectMembershipRole.MEMBER
        );

        assertApiException(
                () -> invitationService.createInvitation(projectId, request),
                HttpStatus.CONFLICT
        );

        verify(membershipRepository)
                .existsByProjectIdAndUserId(projectId, inviteeId);

        verifyNoInteractions(tokenGenerator);

        verify(invitationRepository, never())
                .saveAndFlush(any(ProjectInvitation.class));
    }


    @Test
    void createInvitation_rejectsDuplicatePendingInvitation() {

        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();

        User owner = mock(User.class);
        User invitee = mock(User.class);
        Project project = mock(Project.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(ownerId);
        when(projectAccessService.loadAccessibleProject(projectId, ownerId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(project.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(ownerId);
        when(invitee.getId()).thenReturn(inviteeId);

        when(userRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.of(invitee));

        when(membershipRepository.existsByProjectIdAndUserId(
                projectId, inviteeId
        )).thenReturn(false);

        when(invitationRepository.existsByProject_IdAndInviteeEmailAndStatus(
                projectId,
                "newuser@example.com",
                ProjectInvitationStatus.PENDING
        )).thenReturn(true);

        CreateInvitationRequest request = new CreateInvitationRequest(
                "newuser@example.com",
                ProjectMembershipRole.MEMBER
        );

        assertApiException(
                () -> invitationService.createInvitation(projectId, request),
                HttpStatus.CONFLICT
        );

        verify(invitationRepository)
                .existsByProject_IdAndInviteeEmailAndStatus(
                        projectId,
                        "newuser@example.com",
                        ProjectInvitationStatus.PENDING
                );

        verifyNoInteractions(tokenGenerator);

        verify(invitationRepository, never())
                .saveAndFlush(any(ProjectInvitation.class));
    }


    @Test
    void createInvitation_rejectsMissingRole() {

        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Project project = mock(Project.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(ownerId);
        when(projectAccessService.loadAccessibleProject(projectId, ownerId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);

        CreateInvitationRequest request = new CreateInvitationRequest(
                "newuser@example.com",
                null
        );

        assertApiException(
                () -> invitationService.createInvitation(projectId, request),
                HttpStatus.BAD_REQUEST
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(tokenGenerator);
    }


    @Test
    void createInvitation_rejectsUnknownInviteeEmail() {

        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Project project = mock(Project.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(ownerId);
        when(projectAccessService.loadAccessibleProject(projectId, ownerId))
                .thenReturn(project);

        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        // remove: when(project.getOwner()).thenReturn(owner);
        // remove: when(owner.getId()).thenReturn(ownerId);

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        CreateInvitationRequest request = new CreateInvitationRequest(
                "unknown@example.com",
                ProjectMembershipRole.MEMBER
        );

        assertApiException(
                () -> invitationService.createInvitation(projectId, request),
                HttpStatus.NOT_FOUND
        );

        verify(userRepository).findByEmail("unknown@example.com");
        verifyNoInteractions(tokenGenerator);
    }

    // =========================================================
    // GET MY PENDING INVITATIONS
    // =========================================================

    @Test
    void getMyPendingInvitations_returnsCurrentUsersInvitations() {

        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getEmail()).thenReturn("invitee@example.com");

        Pageable pageable = PageRequest.of(0, 20);

        ProjectInvitation first = mock(ProjectInvitation.class);
        ProjectInvitation second = mock(ProjectInvitation.class);

        MyInvitationResponse firstResponse = mock(MyInvitationResponse.class);
        MyInvitationResponse secondResponse = mock(MyInvitationResponse.class);

        Page<ProjectInvitation> source = new PageImpl<>(
                List.of(first, second),
                pageable,
                2
        );

        when(invitationRepository
                .findAllByInviteeEmailAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                        eq("invitee@example.com"),
                        eq(ProjectInvitationStatus.PENDING),
                        any(Instant.class),
                        eq(pageable)
                )).thenReturn(source);

        when(invitationMapper.toMyInvitationResponse(first))
                .thenReturn(firstResponse);

        when(invitationMapper.toMyInvitationResponse(second))
                .thenReturn(secondResponse);

        Page<MyInvitationResponse> result =
                invitationService.getMyPendingInvitations(pageable);

        assertThat(result.getContent())
                .containsExactly(firstResponse, secondResponse);

        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(invitationMapper).toMyInvitationResponse(first);
        verify(invitationMapper).toMyInvitationResponse(second);
    }


    @Test
    void getMyPendingInvitations_returnsEmptyPageWhenNoInvitationsExist() {

        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getEmail()).thenReturn("invitee@example.com");

        Pageable pageable = PageRequest.of(0, 20);

        when(invitationRepository
                .findAllByInviteeEmailAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                        eq("invitee@example.com"),
                        eq(ProjectInvitationStatus.PENDING),
                        any(Instant.class),
                        eq(pageable)
                )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<MyInvitationResponse> result =
                invitationService.getMyPendingInvitations(pageable);

        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verifyNoInteractions(invitationMapper);
    }


    @Test
    void getMyPendingInvitations_rejectsMissingUser() {

        UUID userId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertApiException(
                () -> invitationService.getMyPendingInvitations(
                        PageRequest.of(0, 20)
                ),
                HttpStatus.NOT_FOUND
        );

        verifyNoInteractions(invitationRepository);
        verifyNoInteractions(invitationMapper);
    }


    @Test
    void getMyPendingInvitations_preservesPaginationMetadata() {

        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getEmail()).thenReturn("invitee@example.com");

        Pageable pageable = PageRequest.of(1, 2);

        ProjectInvitation first = mock(ProjectInvitation.class);
        ProjectInvitation second = mock(ProjectInvitation.class);

        MyInvitationResponse firstResponse = mock(MyInvitationResponse.class);
        MyInvitationResponse secondResponse = mock(MyInvitationResponse.class);

        Page<ProjectInvitation> source = new PageImpl<>(
                List.of(first, second),
                pageable,
                5
        );

        when(invitationRepository
                .findAllByInviteeEmailAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                        eq("invitee@example.com"),
                        eq(ProjectInvitationStatus.PENDING),
                        any(Instant.class),
                        eq(pageable)
                )).thenReturn(source);

        when(invitationMapper.toMyInvitationResponse(first))
                .thenReturn(firstResponse);

        when(invitationMapper.toMyInvitationResponse(second))
                .thenReturn(secondResponse);

        Page<MyInvitationResponse> result =
                invitationService.getMyPendingInvitations(pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }


    // =========================================================
    // ACCEPT INVITATION
    // =========================================================

    @Test
    void acceptInvitation_intendedRecipientCanAccept() {

        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);
        Project project = mock(Project.class);
        ProjectInvitation invitation = mock(ProjectInvitation.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(ProjectInvitationStatus.PENDING);
        when(invitation.getExpiresAt())
                .thenReturn(Instant.now().plus(Duration.ofDays(1)));
        when(invitation.getInviteeEmail()).thenReturn("invitee@example.com");
        when(user.getEmail()).thenReturn("invitee@example.com");

        when(invitation.getProject()).thenReturn(project);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(project.getId()).thenReturn(projectId);
        when(invitation.getRole()).thenReturn(ProjectMembershipRole.MEMBER);

        when(membershipRepository.existsByProjectIdAndUserId(projectId, userId))
                .thenReturn(false);

        when(invitationMapper.toAcceptInvitationResponse(invitation))
                .thenReturn(new AcceptInvitationResponse(
                        invitationId,
                        projectId,
                        "Test project",
                        ProjectMembershipRole.MEMBER,
                        Instant.now()
                ));

        AcceptInvitationResponse result =
                invitationService.acceptInvitation(invitationId);

        assertThat(result).isNotNull();
        assertThat(result.invitationId()).isEqualTo(invitationId);
        assertThat(result.projectId()).isEqualTo(projectId);
        assertThat(result.projectName()).isEqualTo("Test project");
        assertThat(result.role()).isEqualTo(ProjectMembershipRole.MEMBER);

        ArgumentCaptor<ProjectMembership> captor =
                ArgumentCaptor.forClass(ProjectMembership.class);

        verify(membershipRepository).saveAndFlush(captor.capture());

        ProjectMembership savedMembership = captor.getValue();

        assertThat(savedMembership.getProject()).isSameAs(project);
        assertThat(savedMembership.getUser()).isSameAs(user);
        assertThat(savedMembership.getRole())
                .isEqualTo(ProjectMembershipRole.MEMBER);

        verify(invitation).accept(eq(user), any(Instant.class));
        verify(invitationRepository).save(invitation);
    }


    @Test
    void acceptInvitation_rejectsUnknownInvitationId() {

        UUID userId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.empty());

        assertApiException(
                () -> invitationService.acceptInvitation(invitationId),
                HttpStatus.NOT_FOUND
        );

        verifyNoInteractions(membershipRepository);
    }


    @Test
    void acceptInvitation_rejectsDifferentUser() {

        UUID userId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);
        ProjectInvitation invitation = mock(ProjectInvitation.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(ProjectInvitationStatus.PENDING);
        when(invitation.getExpiresAt())
                .thenReturn(Instant.now().plus(Duration.ofDays(1)));
        when(invitation.getInviteeEmail()).thenReturn("invitee@example.com");
        when(user.getEmail()).thenReturn("different@example.com");

        assertApiException(
                () -> invitationService.acceptInvitation(invitationId),
                HttpStatus.FORBIDDEN
        );

        verifyNoInteractions(membershipRepository);
        verify(invitation, never()).accept(any(), any());
        verify(invitationRepository, never()).save(any());
    }


    @Test
    void acceptInvitation_rejectsExpiredInvitation() {

        UUID userId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);
        ProjectInvitation invitation = mock(ProjectInvitation.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(ProjectInvitationStatus.PENDING);
        when(invitation.getExpiresAt())
                .thenReturn(Instant.now().minus(Duration.ofHours(1)));

        assertApiException(
                () -> invitationService.acceptInvitation(invitationId),
                HttpStatus.CONFLICT
        );

        verifyNoInteractions(membershipRepository);
        verify(invitation, never()).accept(any(), any());
    }


    @Test
    void acceptInvitation_rejectsRevokedInvitation() {

        UUID userId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);
        ProjectInvitation invitation = mock(ProjectInvitation.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(ProjectInvitationStatus.REVOKED);

        assertApiException(
                () -> invitationService.acceptInvitation(invitationId),
                HttpStatus.CONFLICT
        );

        verifyNoInteractions(membershipRepository);
        verify(invitation, never()).accept(any(), any());
    }


    @Test
    void acceptInvitation_rejectsAlreadyAcceptedInvitation() {

        UUID userId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);
        ProjectInvitation invitation = mock(ProjectInvitation.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(ProjectInvitationStatus.ACCEPTED);

        assertApiException(
                () -> invitationService.acceptInvitation(invitationId),
                HttpStatus.CONFLICT
        );

        verifyNoInteractions(membershipRepository);
        verify(invitation, never()).accept(any(), any());
    }


    @Test
    void acceptInvitation_rejectsUserWhoIsAlreadyMember() {

        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);
        Project project = mock(Project.class);
        ProjectInvitation invitation = mock(ProjectInvitation.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(ProjectInvitationStatus.PENDING);
        when(invitation.getExpiresAt())
                .thenReturn(Instant.now().plus(Duration.ofDays(1)));
        when(invitation.getInviteeEmail()).thenReturn("invitee@example.com");
        when(user.getEmail()).thenReturn("invitee@example.com");

        when(invitation.getProject()).thenReturn(project);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(project.getId()).thenReturn(projectId);

        when(membershipRepository.existsByProjectIdAndUserId(projectId, userId))
                .thenReturn(true);

        assertApiException(
                () -> invitationService.acceptInvitation(invitationId),
                HttpStatus.CONFLICT
        );

        verify(membershipRepository, never())
                .saveAndFlush(any(ProjectMembership.class));

        verify(invitation, never()).accept(any(), any());
        verify(invitationRepository, never()).save(any());
    }


    @Test
    void acceptInvitation_rejectsArchivedProject() {

        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);
        Project project = mock(Project.class);
        ProjectInvitation invitation = mock(ProjectInvitation.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(ProjectInvitationStatus.PENDING);
        when(invitation.getExpiresAt())
                .thenReturn(Instant.now().plus(Duration.ofDays(1)));
        when(invitation.getInviteeEmail()).thenReturn("invitee@example.com");
        when(user.getEmail()).thenReturn("invitee@example.com");

        when(invitation.getProject()).thenReturn(project);
        when(project.getStatus()).thenReturn(ProjectStatus.ARCHIVED);

        assertApiException(
                () -> invitationService.acceptInvitation(invitationId),
                HttpStatus.CONFLICT
        );

        verifyNoInteractions(membershipRepository);
        verify(invitation, never()).accept(any(), any());
    }


    @Test
    void acceptInvitation_rejectsInvalidRole() {

        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();

        User user = mock(User.class);
        Project project = mock(Project.class);
        ProjectInvitation invitation = mock(ProjectInvitation.class);

        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(invitationRepository.findByIdForUpdate(invitationId))
                .thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(ProjectInvitationStatus.PENDING);
        when(invitation.getExpiresAt())
                .thenReturn(Instant.now().plus(Duration.ofDays(1)));
        when(invitation.getInviteeEmail()).thenReturn("invitee@example.com");
        when(user.getEmail()).thenReturn("invitee@example.com");

        when(invitation.getProject()).thenReturn(project);
        when(project.getStatus()).thenReturn(ProjectStatus.ACTIVE);
        when(project.getId()).thenReturn(projectId);

        when(membershipRepository.existsByProjectIdAndUserId(projectId, userId))
                .thenReturn(false);

        when(invitation.getRole()).thenReturn(null);

        assertApiException(
                () -> invitationService.acceptInvitation(invitationId),
                HttpStatus.CONFLICT
        );

        verify(membershipRepository, never())
                .saveAndFlush(any(ProjectMembership.class));

        verify(invitation, never()).accept(any(), any());
        verify(invitationRepository, never()).save(any());
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private void stubCreateInvitationResponse() {
        when(invitationMapper.toCreateInvitationResponse(
                any(ProjectInvitation.class),
                eq(RAW_TOKEN)
        )).thenAnswer(invocation -> {
            ProjectInvitation saved = invocation.getArgument(0);
            return new CreateInvitationResponse(
                    UUID.randomUUID(),
                    saved.getInviteeEmail(),
                    saved.getRole(),
                    saved.getExpiresAt(),
                    RAW_TOKEN
            );
        });
    }


    private void setInvitationTtl() {
        ReflectionTestUtils.setField(
                invitationService,
                "invitationTtl",
                INVITATION_TTL
        );
    }


    private void verifySuccessfulCreation(
            UUID projectId,
            UUID inviteeId,
            String expectedEmail,
            ProjectMembershipRole expectedRole
    ) {
        verify(tokenGenerator).generateToken();
        verify(tokenGenerator).hashToken(RAW_TOKEN);

        ArgumentCaptor<ProjectInvitation> captor =
                ArgumentCaptor.forClass(ProjectInvitation.class);

        verify(invitationRepository).saveAndFlush(captor.capture());

        ProjectInvitation saved = captor.getValue();

        assertThat(saved.getInviteeEmail()).isEqualTo(expectedEmail);
        assertThat(saved.getRole()).isEqualTo(expectedRole);
        assertThat(saved.getTokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(saved.getTokenHash()).isNotEqualTo(RAW_TOKEN);
        assertThat(saved.getStatus()).isEqualTo(ProjectInvitationStatus.PENDING);

        assertThat(saved.getExpiresAt()).isAfter(saved.getCreatedAt());

        assertThat(Duration.between(
                saved.getCreatedAt(),
                saved.getExpiresAt()
        )).isEqualTo(INVITATION_TTL);

        verify(membershipRepository)
                .existsByProjectIdAndUserId(projectId, inviteeId);
    }


    private void assertApiException(
            Runnable operation,
            HttpStatus expectedStatus
    ) {
        assertThatThrownBy(operation::run)
                .isInstanceOfSatisfying(
                        ApiException.class,
                        exception -> assertThat(exception.getStatus())
                                .isEqualTo(expectedStatus)
                );
    }
}
