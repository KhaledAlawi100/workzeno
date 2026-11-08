
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

import lombok.RequiredArgsConstructor;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectInvitationService {

    private static final String PENDING_INVITATION_CONSTRAINT =
            "uq_project_invitations_pending_project_email";

    private static final String MEMBERSHIP_CONSTRAINT =
            "uq_project_memberships_project_user";

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    private final CurrentUserProvider currentUserProvider;
    private final ProjectAuthorizationService authorizationService;
    private final ProjectAccessService projectAccessService;
    private final TokenGenerator tokenGenerator;
    private final ProjectInvitationMapper invitationMapper;

    @Value("${app.collaboration.invitation.ttl}")
    private Duration invitationTtl;


    @Transactional
    public CreateInvitationResponse createInvitation(
            UUID projectId,
            CreateInvitationRequest request
    ) {
        UUID inviterId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                inviterId
        );

        ensureProjectIsActive(project);

        authorizeInvitation(
                project,
                inviterId,
                request.role()
        );

        String normalizedEmail = normalizeEmail(
                request.inviteeEmail()
        );

        User invitee = loadUserByEmail(normalizedEmail);

        ensureOwnerIsNotInvitee(
                project,
                invitee.getId()
        );

        ensureUserIsNotAlreadyMember(
                projectId,
                invitee.getId()
        );

        ensureNoPendingInvitation(
                projectId,
                normalizedEmail
        );

        String rawToken = tokenGenerator.generateToken();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(invitationTtl);

        User inviter = loadUser(inviterId);

        ProjectInvitation invitation = ProjectInvitation.createPending(
                project,
                normalizedEmail,
                request.role(),
                tokenHash,
                inviter,
                createdAt,
                expiresAt
        );

        try {
            ProjectInvitation savedInvitation =
                    invitationRepository.saveAndFlush(invitation);


            return invitationMapper.toCreateInvitationResponse(
                    savedInvitation,
                    rawToken
            );

        } catch (DataIntegrityViolationException exception) {

            if (isConstraintViolation(
                    exception,
                    PENDING_INVITATION_CONSTRAINT
            )) {
                throw pendingInvitationAlreadyExists();
            }

            throw exception;
        }
    }


    public Page<MyInvitationResponse> getMyPendingInvitations(
            Pageable pageable
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        User currentUser = loadUser(currentUserId);

        Instant now = Instant.now();

        Page<ProjectInvitation> invitations =
                invitationRepository
                        .findAllByInviteeEmailAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                                currentUser.getEmail(),
                                ProjectInvitationStatus.PENDING,
                                now,
                                pageable
                        );

        return invitations.map(
                invitationMapper::toMyInvitationResponse
        );
    }


    @Transactional
    public AcceptInvitationResponse acceptInvitation(
            UUID invitationId
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        User acceptingUser = loadUser(currentUserId);

        ProjectInvitation invitation = invitationRepository
                .findByIdForUpdate(invitationId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Invitation not found",
                        "The requested invitation does not exist"
                ));

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid invitation state",
                    "Only pending invitations can be accepted"
            );
        }

        Instant acceptedAt = Instant.now();

        if (!acceptedAt.isBefore(invitation.getExpiresAt())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invitation expired",
                    "This invitation has expired"
            );
        }

        if (!normalizeEmail(acceptingUser.getEmail())
                .equals(invitation.getInviteeEmail())) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Invitation email mismatch",
                    "This invitation belongs to a different email address"
            );
        }

        Project project = invitation.getProject();

        ensureProjectIsActive(project);

        ensureUserIsNotAlreadyMember(
                project.getId(),
                currentUserId
        );

        ProjectMembershipRole role = invitation.getRole();

        if (role == null) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid invitation role",
                    "The invitation does not contain a valid project role"
            );
        }

        ProjectMembership membership = new ProjectMembership(
                project,
                acceptingUser,
                role
        );

        try {
            membershipRepository.saveAndFlush(membership);

        } catch (DataIntegrityViolationException exception) {

            if (isConstraintViolation(exception, MEMBERSHIP_CONSTRAINT)) {
                throw membershipAlreadyExists();
            }

            throw exception;
        }

        invitation.accept(acceptingUser, acceptedAt);

        invitationRepository.save(invitation);

        return invitationMapper.toAcceptInvitationResponse(invitation);
    }



    @Transactional
    public void declineInvitation(UUID invitationId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        User decliningUser = loadUser(currentUserId);

        ProjectInvitation invitation = invitationRepository
                .findByIdForUpdate(invitationId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Invitation not found",
                        "The requested invitation does not exist"
                ));

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid invitation state",
                    "Only pending invitations can be declined"
            );
        }

        Instant declinedAt = Instant.now();

        if (!declinedAt.isBefore(invitation.getExpiresAt())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invitation expired",
                    "This invitation has expired"
            );
        }

        if (!normalizeEmail(decliningUser.getEmail())
                .equals(invitation.getInviteeEmail())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Invitation email mismatch",
                    "Only the intended recipient can decline this invitation"
            );
        }

        invitation.decline(decliningUser, declinedAt);
        invitationRepository.save(invitation);
    }


    @Transactional
    public void revokeInvitation(UUID projectId, UUID invitationId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        ensureProjectIsActive(project);

        ProjectInvitation invitation = invitationRepository
                .findByIdForUpdate(invitationId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Invitation not found",
                        "The requested invitation does not exist"
                ));

        if (!projectId.equals(invitation.getProject().getId())) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Invitation not found",
                    "The requested invitation does not belong to this project"
            );
        }

        if (invitation.getRole() == null) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid invitation role",
                    "The invitation does not contain a valid project role"
            );
        }

        // Mirror creation permissions: only the owner can revoke an ADMIN
        // invitation; an owner or project ADMIN can revoke a MEMBER invitation.
        if (invitation.getRole() == ProjectMembershipRole.ADMIN) {
            authorizationService.requireOwner(project, currentUserId);
        } else {
            authorizationService.requireOwnerOrAdmin(project, currentUserId);
        }

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid invitation state",
                    "Only pending invitations can be revoked"
            );
        }

        Instant revokedAt = Instant.now();

        if (!revokedAt.isBefore(invitation.getExpiresAt())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invitation expired",
                    "An expired invitation cannot be revoked"
            );
        }

        User revokingUser = loadUser(currentUserId);
        invitation.revoke(revokingUser, revokedAt);
        invitationRepository.save(invitation);
    }


    private void authorizeInvitation(
            Project project,
            UUID inviterId,
            ProjectMembershipRole requestedRole
    ) {
        if (requestedRole == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid invitation role",
                    "An invitation role is required"
            );
        }

        if (requestedRole == ProjectMembershipRole.ADMIN) {
            authorizationService.requireOwner(
                    project,
                    inviterId
            );
            return;
        }

        // Owner or ADMIN can invite someone as MEMBER.
        authorizationService.requireOwnerOrAdmin(
                project,
                inviterId
        );
    }

    private void ensureProjectIsActive(Project project) {
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Project is archived",
                    "Invitation operations are not allowed for archived projects"
            );
        }
    }

    private void ensureOwnerIsNotInvitee(Project project, UUID inviteeId) {
        if (Objects.equals(project.getOwner().getId(), inviteeId)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid invitation",
                    "The project owner cannot be invited as a member"
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email",
                    "An invitee email address is required"
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureUserIsNotAlreadyMember(UUID projectId, UUID userId
    ) {
        if (membershipRepository.existsByProjectIdAndUserId(
                projectId,
                userId
        )) {
            throw membershipAlreadyExists();
        }
    }

    private void ensureNoPendingInvitation(UUID projectId, String normalizedEmail) {
        boolean invitationExists =
                invitationRepository
                        .existsByProject_IdAndInviteeEmailAndStatus(
                                projectId,
                                normalizedEmail,
                                ProjectInvitationStatus.PENDING
                        );

        if (invitationExists) {
            throw pendingInvitationAlreadyExists();
        }
    }

    private User loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found",
                        "No Workzeno user exists with the supplied email address"
                ));
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found",
                        "The requested user account could not be found"
                ));
    }

    private ApiException membershipAlreadyExists() {
        return new ApiException(
                HttpStatus.CONFLICT,
                "Membership conflict",
                "This user is already a member of the project"
        );
    }

    private ApiException pendingInvitationAlreadyExists() {
        return new ApiException(
                HttpStatus.CONFLICT,
                "Invitation conflict",
                "A pending invitation already exists for this user and project"
        );
    }

    private boolean isConstraintViolation(Throwable exception, String constraintName) {
        Throwable current = exception;

        while (current != null) {

            if (current instanceof ConstraintViolationException violation) {
                return constraintName.equalsIgnoreCase(
                        violation.getConstraintName()
                );
            }

            current = current.getCause();
        }

        return false;
    }
}
