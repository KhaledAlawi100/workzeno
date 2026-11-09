package com.workzeno.workzeno.collaboration.repository;

import com.workzeno.workzeno.collaboration.domain.ProjectMembership;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectMembershipRepository
        extends JpaRepository<ProjectMembership, UUID> {

    Optional<ProjectMembership> findByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT membership
        FROM ProjectMembership membership
        WHERE membership.project.id = :projectId
          AND membership.user.id = :userId
        """)
    Optional<ProjectMembership> findByProjectIdAndUserIdForUpdate(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

    @EntityGraph(attributePaths = "user")
    Page<ProjectMembership> findAllByProjectIdOrderByJoinedAtDesc(
            UUID projectId,
            Pageable pageable
    );

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
}