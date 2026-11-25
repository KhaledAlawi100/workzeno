
package com.workzeno.workzeno.collaboration.invitation.service;

import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitation;
import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitationStatus;
import com.workzeno.workzeno.collaboration.invitation.dto.*;
import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;
import com.workzeno.workzeno.collaboration.invitation.exception.InvitationExpiredException;
import com.workzeno.workzeno.collaboration.invitation.mapper.ProjectInvitationMapper;
import com.workzeno.workzeno.collaboration.invitation.repository.ProjectInvitationRepository;
import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.EmailAddress;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.service.ProjectAccessService;
import com.workzeno.workzeno.projects.service.ProjectModificationPolicy;
import com.workzeno.workzeno.shared.exception.ApiException;
import com.workzeno.workzeno.collaboration.membership.api.MembershipManagement;
import com.workzeno.workzeno.collaboration.membership.api.ProjectAuthorization;

import com.workzeno.workzeno.notification.domain.NotificationType;
import com.workzeno.workzeno.notification.event.NotificationEvent;

import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectInvitationService {

    private static final String PENDING_INVITATION_CONSTRAINT =
            "uq_project_invitations_pending_project_email";


    private static final int INVITATION_EXPIRATION_BATCH_SIZE = 100;

    private final ProjectInvitationRepository invitationRepository;
    private final MembershipManagement membershipManagement;
    private final UserRepository userRepository;

    private final CurrentUserProvider currentUserProvider;
    private final ProjectAuthorization authorizationService;
    private final ProjectAccessService projectAccessService;
    private final ProjectInvitationMapper invitationMapper;
    private final ProjectModificationPolicy projectModificationPolicy;
    private final ApplicationEventPublisher eventPublisher;


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

        projectModificationPolicy.requireActive(project, "Invitation");

        authorizeInvitation(
                project,
                inviterId,
                request.role()
        );

        String normalizedEmail = EmailAddress.normalize(
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

            eventPublisher.publishEvent(new NotificationEvent(
                    invitee.getId(),
                    inviter.getId(),
                    NotificationType.PROJECT_INVITATION,
                    "Project invitation",
                    "You were invited to join \"" + project.getName() + "\".",
                    "/invitations",
                    "PROJECT_INVITATION",
                    savedInvitation.getId()
            ));


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
    // MY PENDING INVITATIONS
    // =========================================================

    @Transactional
    public Page<MyInvitationResponse> getMyPendingInvitations(
            Pageable pageable
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        User currentUser = loadUser(currentUserId);

        Instant now = Instant.now();

        // Expire invitations before returning pending invitations.
        expireRecipientInvitations(
                currentUser.getEmail(),
                now
        );

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

        if (!EmailAddress.normalize(acceptingUser.getEmail())
                .equals(invitation.getInviteeEmail())) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Invitation email mismatch",
                    "This invitation belongs to a different email address"
            );
        }

        Instant acceptedAt = Instant.now();

        // If expired, update the invitation and throw the domain exception.
        expireInvitationIfNecessary(invitation, acceptedAt);

        Project project = invitation.getProject();

        projectModificationPolicy.requireActive(project, "Invitation");

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
                role
        );

        invitation.accept(
                acceptingUser,
                acceptedAt
        );

        invitationRepository.save(invitation);

        eventPublisher.publishEvent(new NotificationEvent(
                invitation.getInvitedBy().getId(),
                acceptingUser.getId(),
                NotificationType.INVITATION_ACCEPTED,
                "Invitation accepted",
                acceptingUser.getEmail()
                        + " accepted your invitation to join \""
                        + project.getName() + "\".",
                "/projects/" + project.getId(),
                "PROJECT_INVITATION",
                invitation.getId()
        ));

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

        if (!EmailAddress.normalize(decliningUser.getEmail())
                .equals(invitation.getInviteeEmail())) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Invitation email mismatch",
                    "Only the intended recipient can decline this invitation"
            );
        }

        Instant declinedAt = Instant.now();

        expireInvitationIfNecessary(
                invitation,
                declinedAt
        );

        invitation.decline(
                decliningUser,
                declinedAt
        );

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

        projectModificationPolicy.requireActive(project, "Invitation");

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
            authorizationService.requireOwner(
                    project,
                    currentUserId
            );
        } else {
            authorizationService.requireOwnerOrAdmin(
                    project,
                    currentUserId
            );
        }

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid invitation state",
                    "Only pending invitations can be revoked"
            );
        }

        Instant revokedAt = Instant.now();

        expireInvitationIfNecessary(
                invitation,
                revokedAt
        );

        User revokingUser = loadUser(currentUserId);

        invitation.revoke(
                revokingUser,
                revokedAt
        );

        invitationRepository.save(invitation);
    }

    // =========================================================
    // PROJECT INVITATION HISTORY
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

        // Expire old invitations before returning project history.
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
    // SINGLE PROJECT INVITATION
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
                .findByIdAndProject_Id(
                        invitationId,
                        projectId
                )
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Invitation not found",
                        "The requested invitation does not exist in this project"
                ));

        return invitationMapper.toProjectInvitationResponse(invitation);
    }

    // =========================================================
    // BATCH EXPIRATION
    // =========================================================

    /**
     * Expires invitations for a project and a specific email.
     *
     * Each query retrieves at most BATCH_SIZE invitation IDs.
     * Entities are loaded and updated in bounded batches so
     * expiration does not load an unlimited number of rows.
     */
    private void expireExpiredInvitationForProjectAndEmail(
            UUID projectId,
            String inviteeEmail,
            Instant now
    ) {
        while (true) {

            List<UUID> expiredIds =
                    invitationRepository
                            .findExpiredProjectRecipientInvitationIds(
                                    projectId,
                                    inviteeEmail,
                                    ProjectInvitationStatus.PENDING,
                                    now,
                                    PageRequest.of(
                                            0,
                                            INVITATION_EXPIRATION_BATCH_SIZE
                                    )
                            );

            if (expiredIds.isEmpty()) {
                return;
            }

            expireInvitationBatch(
                    expiredIds,
                    now
            );
        }
    }


    private void expireProjectInvitations(
            UUID projectId,
            Instant now
    ) {
        while (true) {

            List<UUID> expiredIds =
                    invitationRepository
                            .findExpiredProjectInvitationIds(
                                    projectId,
                                    ProjectInvitationStatus.PENDING,
                                    now,
                                    PageRequest.of(
                                            0,
                                            INVITATION_EXPIRATION_BATCH_SIZE
                                    )
                            );

            if (expiredIds.isEmpty()) {
                return;
            }

            expireInvitationBatch(
                    expiredIds,
                    now
            );
        }
    }

    /**
     * Expires all expired pending invitations belonging to a recipient.
     */
    private void expireRecipientInvitations(
            String inviteeEmail,
            Instant now
    ) {
        while (true) {

            List<UUID> expiredIds =
                    invitationRepository
                            .findExpiredRecipientInvitationIds(
                                    inviteeEmail,
                                    ProjectInvitationStatus.PENDING,
                                    now,
                                    PageRequest.of(
                                            0,
                                            INVITATION_EXPIRATION_BATCH_SIZE
                                    )
                            );

            if (expiredIds.isEmpty()) {
                return;
            }

            expireInvitationBatch(
                    expiredIds,
                    now
            );
        }
    }


    private void expireInvitationBatch(
            List<UUID> invitationIds,
            Instant now
    ) {
        if (invitationIds.isEmpty()) {
            return;
        }

        List<ProjectInvitation> invitations =
                invitationRepository.findAllById(invitationIds);

        if (invitations.isEmpty()) {
            return;
        }

        for (ProjectInvitation invitation : invitations) {

            if (invitation.getStatus() == ProjectInvitationStatus.PENDING
                    && !now.isBefore(invitation.getExpiresAt())) {

                invitation.expire(now);
            }
        }


        invitationRepository.saveAllAndFlush(invitations);
    }

    // =========================================================
    // SINGLE INVITATION EXPIRATION
    // =========================================================

    private void expireInvitationIfNecessary(
            ProjectInvitation invitation,
            Instant now
    ) {
        if (!now.isBefore(invitation.getExpiresAt())) {

            invitation.expire(now);

            invitationRepository.save(invitation);

            // This exception is configured not to roll back
            // so the EXPIRED state is persisted.
            throw new InvitationExpiredException();
        }
    }

    // =========================================================
    // AUTHORIZATION
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

        authorizationService.requireOwnerOrAdmin(
                project,
                inviterId
        );
    }

    // =========================================================
    // VALIDATION HELPERS
    // =========================================================

    private void ensureOwnerIsNotInvitee(
            Project project,
            UUID inviteeId
    ) {
        if (Objects.equals(
                project.getOwner().getId(),
                inviteeId
        )) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid invitation",
                    "The project owner cannot be invited as a member"
            );
        }
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
    // USER LOOKUP
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
    // EXCEPTION HELPERS
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

    /**
     * Searches the exception cause chain for a database constraint
     * violation with the specified constraint name.
     */
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
