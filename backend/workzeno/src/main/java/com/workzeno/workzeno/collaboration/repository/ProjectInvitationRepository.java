package com.workzeno.workzeno.collaboration.repository;

import com.workzeno.workzeno.collaboration.domain.ProjectInvitation;
import com.workzeno.workzeno.collaboration.domain.ProjectInvitationStatus;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectInvitationRepository
        extends JpaRepository<ProjectInvitation, UUID> {

    Optional<ProjectInvitation> findByTokenHash(String tokenHash);

    boolean existsByProject_IdAndInviteeEmailAndStatus(
            UUID projectId,
            String inviteeEmail,
            ProjectInvitationStatus status
    );

    List<ProjectInvitation> findAllByProject_IdAndStatusOrderByCreatedAtDesc(
            UUID projectId,
            ProjectInvitationStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT invitation
            FROM ProjectInvitation invitation
            WHERE invitation.tokenHash = :tokenHash
            """)
    Optional<ProjectInvitation> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );


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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT invitation
        FROM ProjectInvitation invitation
        WHERE invitation.id = :invitationId
        """)
    Optional<ProjectInvitation> findByIdForUpdate(
            @Param("invitationId") UUID invitationId
    );


}
