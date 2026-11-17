
package com.workzeno.workzeno.collaboration.invitation.service;
import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitation;
import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitationStatus;
import com.workzeno.workzeno.collaboration.invitation.dto.*;
import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;
import com.workzeno.workzeno.collaboration.invitation.exception.InvitationExpiredException;
import com.workzeno.workzeno.collaboration.invitation.mapper.ProjectInvitationMapper;
import com.workzeno.workzeno.collaboration.invitation.repository.ProjectInvitationRepository;
import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.domain.ProjectStatus;
import com.workzeno.workzeno.projects.service.ProjectAccessService;
import com.workzeno.workzeno.shared.exception.ApiException;
import com.workzeno.workzeno.collaboration.membership.api.MembershipManagement;
import com.workzeno.workzeno.collaboration.membership.api.ProjectAuthorization;

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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectInvitationService {

    private static final String PENDING_INVITATION_CONSTRAINT =
            "uq_project_invitations_pending_project_email";

    private final ProjectInvitationRepository invitationRepository;
    private final MembershipManagement membershipManagement;
    private final UserRepository userRepository;

    private final CurrentUserProvider currentUserProvider;
    private final ProjectAuthorization authorizationService;
    private final ProjectAccessService projectAccessService;
    private final ProjectInvitationMapper invitationMapper;

    @Value("${app.collaboration.invitation.ttl}")
    private Duration invitationTtl;


    // =========================================================
    // CREATE INVITATION
    // =========================================================

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

        expireExpiredInvitationForProjectAndEmail(
                projectId,
                normalizedEmail,
                Instant.now()
        );


        ensureNoPendingInvitation(
                projectId,
                normalizedEmail
        );



        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(invitationTtl);

        User inviter = loadUser(inviterId);

        ProjectInvitation invitation = ProjectInvitation.createPending(
                project,
                normalizedEmail,
                request.role(),
                inviter,
                createdAt,
                expiresAt
        );

        try {
            ProjectInvitation savedInvitation =
                    invitationRepository.saveAndFlush(invitation);

            return invitationMapper.toCreateInvitationResponse(
                    savedInvitation
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


    // =========================================================
    // GET MY PENDING INVITATIONS
    // =========================================================

    @Transactional
    public Page<MyInvitationResponse> getMyPendingInvitations(
            Pageable pageable
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        User currentUser = loadUser(currentUserId);

        Instant now = Instant.now();

        // First, persist EXPIRED for invitations that have expired.
        expireRecipientInvitations(
                currentUser.getEmail(),
                now
        );

        // Then retrieve only invitations that are still pending
        // and have not expired.
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


    // =========================================================
    // ACCEPT INVITATION
    // =========================================================

    @Transactional(noRollbackFor = InvitationExpiredException.class)
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

        if (!normalizeEmail(acceptingUser.getEmail())
                .equals(invitation.getInviteeEmail())) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Invitation email mismatch",
                    "This invitation belongs to a different email address"
            );
        }

        Instant acceptedAt = Instant.now();

        // If expired, this changes the status to EXPIRED,
        // saves it, and throws InvitationExpiredException.
        // The transaction commits because of noRollbackFor above.
        expireInvitationIfNecessary(invitation, acceptedAt);

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

        membershipManagement.addMemberFromInvitation(
                project,
                acceptingUser,
                role);

        invitation.accept(acceptingUser, acceptedAt);


        invitationRepository.save(invitation);

        return invitationMapper.toAcceptInvitationResponse(invitation);
    }


    // =========================================================
    // DECLINE INVITATION
    // =========================================================

    @Transactional(noRollbackFor = InvitationExpiredException.class)
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

        // Verify that the current user is the intended recipient.
        if (!normalizeEmail(decliningUser.getEmail())
                .equals(invitation.getInviteeEmail())) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Invitation email mismatch",
                    "Only the intended recipient can decline this invitation"
            );
        }

        Instant declinedAt = Instant.now();

        // Persist EXPIRED and stop if the invitation has expired.
        expireInvitationIfNecessary(invitation, declinedAt);

        // The invitation is still valid, so it can be declined.
        invitation.decline(decliningUser, declinedAt);

        invitationRepository.save(invitation);
    }


    // =========================================================
    // REVOKE INVITATION
    // =========================================================

    @Transactional(noRollbackFor = InvitationExpiredException.class)
    public void revokeInvitation(
            UUID projectId,
            UUID invitationId
    ) {
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

        // Persist EXPIRED and stop if the invitation has expired.
        expireInvitationIfNecessary(invitation, revokedAt);

        User revokingUser = loadUser(currentUserId);

        // The invitation is still valid, so it can be revoked.
        // The domain method records revokedBy and revokedAt.
        invitation.revoke(revokingUser, revokedAt);

        invitationRepository.save(invitation);
    }


    // =========================================================
    // GET PROJECT INVITATIONS
    // =========================================================

    @Transactional
    public Page<ProjectInvitationSummaryResponse> getProjectInvitations(
            UUID projectId,
            Pageable pageable
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        authorizationService.requireOwnerOrAdmin(
                project,
                currentUserId
        );


        expireProjectInvitations(
                projectId,
                Instant.now()
        );

        Page<ProjectInvitation> invitations =
                invitationRepository
                        .findAllByProject_IdOrderByCreatedAtDesc(
                                projectId,
                                pageable
                        );

        return invitations.map(
                invitationMapper::toProjectInvitationSummaryResponse
        );
    }


    // =========================================================
    // GET PROJECT INVITATION DETAILS
    // =========================================================

    @Transactional(readOnly = true)
    public ProjectInvitationResponse getProjectInvitation(
            UUID projectId,
            UUID invitationId
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        authorizationService.requireOwnerOrAdmin(
                project,
                currentUserId
        );

        ProjectInvitation invitation = invitationRepository
                .findByIdAndProject_Id(invitationId, projectId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Invitation not found",
                        "The requested invitation does not exist in this project"
                ));

        // Convert the entity into the complete invitation details response.
        return invitationMapper.toProjectInvitationResponse(invitation);
    }



    private void expireExpiredInvitationForProjectAndEmail(
            UUID projectId,
            String inviteeEmail,
            Instant now
    ) {

        List<ProjectInvitation> expiredInvitations =
                invitationRepository
                        .findAllByProject_IdAndInviteeEmailAndStatusAndExpiresAtLessThanEqual(
                                projectId,
                                inviteeEmail,
                                ProjectInvitationStatus.PENDING,
                                now
                        );


        if (expiredInvitations.isEmpty()) {
            return;
        }

        expiredInvitations.forEach(
                invitation -> invitation.expire(now)
        );


        invitationRepository.saveAllAndFlush(expiredInvitations);
    }

    // =========================================================
    // EXPIRATION HELPERS
    // =========================================================


    /**
     * Expires all pending invitations belonging to a project
     * whose expiration time has been reached.
     */
    private void expireProjectInvitations(
            UUID projectId,
            Instant now
    ) {
        List<ProjectInvitation> expiredInvitations =
                invitationRepository
                        .findAllByProject_IdAndStatusAndExpiresAtLessThanEqual(
                                projectId,
                                ProjectInvitationStatus.PENDING,
                                now
                        );

        expiredInvitations.forEach(
                invitation -> invitation.expire(now)
        );

        invitationRepository.saveAll(expiredInvitations);
    }


    /**
     * Expires all pending invitations belonging to a recipient
     * whose expiration time has been reached.
     */
    private void expireRecipientInvitations(
            String inviteeEmail,
            Instant now
    ) {
        List<ProjectInvitation> expiredInvitations =
                invitationRepository
                        .findAllByInviteeEmailAndStatusAndExpiresAtLessThanEqual(
                                inviteeEmail,
                                ProjectInvitationStatus.PENDING,
                                now
                        );

        expiredInvitations.forEach(
                invitation -> invitation.expire(now)
        );

        invitationRepository.saveAll(expiredInvitations);
    }


    private void expireInvitationIfNecessary(
            ProjectInvitation invitation,
            Instant now
    ) {
        if (!now.isBefore(invitation.getExpiresAt())) {

            invitation.expire(now);

            invitationRepository.save(invitation);

            throw new InvitationExpiredException();
        }
    }


    // =========================================================
    // AUTHORIZATION AND VALIDATION HELPERS
    // =========================================================

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


    private void ensureOwnerIsNotInvitee(
            Project project,
            UUID inviteeId
    ) {
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



    private void ensureUserIsNotAlreadyMember(
            UUID projectId,
            UUID userId
    ) {
        if (membershipManagement.isMember(projectId, userId)) {
            throw membershipAlreadyExists();
        }
    }


    private void ensureNoPendingInvitation(
            UUID projectId,
            String normalizedEmail
    ) {
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


    // =========================================================
    // USER LOOKUP HELPERS
    // =========================================================

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


    // =========================================================
    // ERROR HELPERS
    // =========================================================

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


    private boolean isConstraintViolation(
            Throwable exception,
            String constraintName
    ) {
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
