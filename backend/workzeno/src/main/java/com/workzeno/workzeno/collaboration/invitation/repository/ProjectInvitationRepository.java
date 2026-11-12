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
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<ProjectInvitation> findAllByProject_IdAndStatusOrderByCreatedAtDesc(
            UUID projectId,
            ProjectInvitationStatus status
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



    @EntityGraph(attributePaths = "invitedBy")
    Page<ProjectInvitation> findAllByProject_IdOrderByCreatedAtDesc(
            UUID projectId,
            Pageable pageable
    );



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProjectInvitation>
    findAllByProject_IdAndStatusAndExpiresAtLessThanEqual(
            UUID projectId,
            ProjectInvitationStatus status,
            Instant now
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProjectInvitation>
    findAllByInviteeEmailAndStatusAndExpiresAtLessThanEqual(
            String inviteeEmail,
            ProjectInvitationStatus status,
            Instant now
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProjectInvitation>
    findAllByProject_IdAndInviteeEmailAndStatusAndExpiresAtLessThanEqual(
            UUID projectId,
            String inviteeEmail,
            ProjectInvitationStatus status,
            Instant now
    );


    @EntityGraph(attributePaths = {
            "invitedBy",
            "revokedBy"
    })
    Optional<ProjectInvitation> findByIdAndProject_Id(
            UUID invitationId,
            UUID projectId
    );




}
