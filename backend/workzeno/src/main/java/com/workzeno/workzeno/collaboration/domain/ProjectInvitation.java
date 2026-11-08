package com.workzeno.workzeno.collaboration.domain;

import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.shared.exception.ApiException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "project_invitations")
@Getter
public class ProjectInvitation {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_project_invitations_project")
    )
    private Project project;

    @Column(name = "invitee_email", nullable = false, length = 254)
    private String inviteeEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ProjectMembershipRole role;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "invited_by_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_project_invitations_invited_by")
    )
    private User invitedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "accepted_by_user_id",
            foreignKey = @ForeignKey(name = "fk_project_invitations_accepted_by")
    )
    private User acceptedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectInvitationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    protected ProjectInvitation() {
        // Required by JPA.
    }

    private ProjectInvitation(
            Project project,
            String inviteeEmail,
            ProjectMembershipRole role,
            String tokenHash,
            User invitedBy,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.project = project;
        this.inviteeEmail = normalizeEmail(inviteeEmail);
        this.role = role;
        this.tokenHash = tokenHash;
        this.invitedBy = invitedBy;
        this.status = ProjectInvitationStatus.PENDING;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;

        validateCreation();
    }

    public static ProjectInvitation createPending(
            Project project,
            String inviteeEmail,
            ProjectMembershipRole role,
            String tokenHash,
            User invitedBy,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new ProjectInvitation(
                project,
                inviteeEmail,
                role,
                tokenHash,
                invitedBy,
                createdAt,
                expiresAt
        );
    }

    public void accept(User acceptingUser, Instant acceptedAt) {
        requirePending();

        if (acceptingUser == null || acceptedAt == null) {
            throw new IllegalArgumentException(
                    "Accepting user and acceptance time are required"
            );
        }

        if (!normalizeEmail(acceptingUser.getEmail()).equals(inviteeEmail)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Invitation email mismatch",
                    "This invitation belongs to a different email address"
            );
        }

        if (!acceptedAt.isBefore(expiresAt)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invitation expired",
                    "This invitation has expired"
            );
        }

        this.acceptedBy = acceptingUser;
        this.acceptedAt = acceptedAt;
        this.status = ProjectInvitationStatus.ACCEPTED;
    }

    public void decline(User decliningUser, Instant declinedAt) {
        requirePending();

        if (decliningUser == null || declinedAt == null) {
            throw new IllegalArgumentException(
                    "Declining user and decline time are required"
            );
        }

        if (!normalizeEmail(decliningUser.getEmail()).equals(inviteeEmail)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Invitation email mismatch",
                    "This invitation belongs to a different email address"
            );
        }

        if (!declinedAt.isBefore(expiresAt)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invitation expired",
                    "This invitation has expired"
            );
        }


        this.status = ProjectInvitationStatus.DECLINED;
    }

    public void revoke(User revokingUser, Instant revokedAt) {
        requirePending();

        if (revokingUser == null || revokedAt == null) {
            throw new IllegalArgumentException(
                    "Revoking user and revocation time are required"
            );
        }

        if (!revokedAt.isBefore(expiresAt)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invitation expired",
                    "An expired invitation cannot be revoked"
            );
        }

        this.status = ProjectInvitationStatus.REVOKED;
    }

    public void expire(Instant now) {
        requirePending();

        if (now == null) {
            throw new IllegalArgumentException(
                    "Current time is required"
            );
        }

        if (now.isBefore(expiresAt)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invitation has not expired",
                    "A pending invitation cannot expire before its expiration time"
            );
        }

        this.status = ProjectInvitationStatus.EXPIRED;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private void validateCreation() {
        if (project == null) {
            throw new IllegalArgumentException("Project is required");
        }

        if (invitedBy == null) {
            throw new IllegalArgumentException("Inviter is required");
        }

        if (role == null) {
            throw new IllegalArgumentException("Invitation role is required");
        }

        if (tokenHash == null || !tokenHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(
                    "Token hash must be a lowercase SHA-256 hexadecimal value"
            );
        }

        if (createdAt == null || expiresAt == null
                || !expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "Expiration must be after creation"
            );
        }
    }

    private void requirePending() {
        if (status != ProjectInvitationStatus.PENDING) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Invalid invitation state",
                    "Only pending invitations can change state"
            );
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
