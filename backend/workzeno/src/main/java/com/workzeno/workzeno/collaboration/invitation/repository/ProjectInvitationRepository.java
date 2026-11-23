
package com.workzeno.workzeno.collaboration.invitation.repository;

import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitation;
import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitationStatus;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectInvitationRepository
        extends JpaRepository<ProjectInvitation, UUID> {

    boolean existsByProject_IdAndInviteeEmailAndStatus(
            UUID projectId,
            String inviteeEmail,
            ProjectInvitationStatus status
    );

    // =========================================================
    // INVITATION HISTORY
    // =========================================================

    /**
     * Returns the current user's pending invitations.
     *
     * Project and inviter are fetched together to prevent
     * lazy-loading queries while mapping the response.
     */
    @EntityGraph(attributePaths = {
            "project",
            "invitedBy"
    })
    Page<ProjectInvitation>
    findAllByInviteeEmailAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
            String inviteeEmail,
            ProjectInvitationStatus status,
            Instant now,
            Pageable pageable
    );

    /**
     * Retrieves a specific invitation while locking its row.
     *
     * Prevents concurrent accept/decline/revoke operations
     * from changing the invitation simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT invitation
            FROM ProjectInvitation invitation
            WHERE invitation.id = :invitationId
            """)
    Optional<ProjectInvitation> findByIdForUpdate(
            @Param("invitationId") UUID invitationId
    );


    @EntityGraph(attributePaths = {
            "invitedBy",
            "revokedBy"
    })
    Page<ProjectInvitation> findAllByProject_IdOrderByCreatedAtDesc(
            UUID projectId,
            Pageable pageable
    );

    // =========================================================
    // BOUNDED EXPIRATION QUERIES
    // =========================================================

    /**
     * Returns only expired invitation IDs for a project.
     *
     * Pageable limits each expiration batch. Ordering is
     * deterministic so batches are processed consistently.
     */
    @Query("""
            SELECT invitation.id
            FROM ProjectInvitation invitation
            WHERE invitation.project.id = :projectId
              AND invitation.status = :status
              AND invitation.expiresAt <= :now
            ORDER BY invitation.expiresAt ASC, invitation.id ASC
            """)
    List<UUID> findExpiredProjectInvitationIds(
            @Param("projectId") UUID projectId,
            @Param("status") ProjectInvitationStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );

    /**
     * Returns only expired invitation IDs for a recipient.
     */
    @Query("""
            SELECT invitation.id
            FROM ProjectInvitation invitation
            WHERE invitation.inviteeEmail = :inviteeEmail
              AND invitation.status = :status
              AND invitation.expiresAt <= :now
            ORDER BY invitation.expiresAt ASC, invitation.id ASC
            """)
    List<UUID> findExpiredRecipientInvitationIds(
            @Param("inviteeEmail") String inviteeEmail,
            @Param("status") ProjectInvitationStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );

    /**
     * Returns only expired invitation IDs for a project
     * and a specific recipient.
     */
    @Query("""
            SELECT invitation.id
            FROM ProjectInvitation invitation
            WHERE invitation.project.id = :projectId
              AND invitation.inviteeEmail = :inviteeEmail
              AND invitation.status = :status
              AND invitation.expiresAt <= :now
            ORDER BY invitation.expiresAt ASC, invitation.id ASC
            """)
    List<UUID> findExpiredProjectRecipientInvitationIds(
            @Param("projectId") UUID projectId,
            @Param("inviteeEmail") String inviteeEmail,
            @Param("status") ProjectInvitationStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );


   /**
     * Retrieves an invitation scoped to its project.
     *
     * Inviter and revoker are fetched together for response mapping.
     */
    @EntityGraph(attributePaths = {
            "invitedBy",
            "revokedBy"
    })
    Optional<ProjectInvitation> findByIdAndProject_Id(
            UUID invitationId,
            UUID projectId
    );
}
