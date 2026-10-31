
package com.workzeno.workzeno.projects.repository;

import com.workzeno.workzeno.projects.domain.Project;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Retrieves a paginated list of projects accessible to a given user.
     * <p>
     * A project is considered accessible if:
     * <ul>
     *   <li>The user is the direct owner of the project ({@code owner_id = :userId}), OR</li>
     *   <li>The user is an active member of the project via the {@code project_memberships} table.</li>
     * </ul>
     * </p>
     * <p>
     * Results are ordered by creation date descending ({@code created_at DESC}), with project ID
     * ({@code id DESC}) used as a secondary tie-breaker.
     * </p>
     *
     * @param userId   the {@link UUID} of the user whose accessible projects are being requested
     * @param pageable pagination and sorting information (page number, page size)
     * @return a {@link Page} of {@link Project} entities accessible to the specified user
     */
    @Query(
            value = """
            SELECT p.*
            FROM projects p
            WHERE p.owner_id = :userId
               OR EXISTS (
                   SELECT 1
                   FROM project_memberships pm
                   WHERE pm.project_id = p.id
                     AND pm.user_id = :userId
               )
            ORDER BY p.created_at DESC, p.id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM projects p
            WHERE p.owner_id = :userId
               OR EXISTS (
                   SELECT 1
                   FROM project_memberships pm
                   WHERE pm.project_id = p.id
                     AND pm.user_id = :userId
               )
            """,
            nativeQuery = true
    )
    Page<Project> findAccessibleProjects(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query(value = """
        SELECT p.*
        FROM projects p
        WHERE p.id = :projectId
          AND (
              p.owner_id = :userId
              OR EXISTS (
                  SELECT 1
                  FROM  project_memberships pm
                  WHERE pm.project_id = p.id
                    AND pm.user_id = :userId
              )
          )
        """, nativeQuery = true)
    Optional<Project> findAccessibleProjectById(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );
}
