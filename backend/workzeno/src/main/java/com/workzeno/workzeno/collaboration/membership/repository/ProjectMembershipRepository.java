package com.workzeno.workzeno.collaboration.membership.repository;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembership;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
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


    @Query("""
    SELECT membership.role
    FROM ProjectMembership membership
    WHERE membership.project.id = :projectId
      AND membership.user.id = :userId
    """)
    Optional<ProjectMembershipRole> findRoleByProjectIdAndUserId(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

    @Query("""
        SELECT membership
        FROM ProjectMembership membership
        JOIN FETCH membership.user
        WHERE membership.project.id = :projectId
          AND membership.user.id IN :userIds
        """)
    List<ProjectMembership> findAllByProjectIdAndUserIds(
            @Param("projectId") UUID projectId,
            @Param("userIds") Collection<UUID> userIds
    );


    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);

    /**
     * Search members belonging to one project by email.
     *
     * Only project memberships are returned.
     * The associated User is fetched in the same query so that
     * mapping the response does not trigger lazy-loading queries.
     *
     * Pageable limits the number of suggestions returned.
     */
    @Query("""
        SELECT membership
        FROM ProjectMembership membership
        JOIN FETCH membership.user user
        WHERE membership.project.id = :projectId
          AND LOWER(user.email) LIKE LOWER(CONCAT(:query, '%'))
        ORDER BY user.email ASC
        """)
    List<ProjectMembership> searchMembersByEmail(
            @Param("projectId") UUID projectId,
            @Param("query") String query,
            Pageable pageable
    );


}