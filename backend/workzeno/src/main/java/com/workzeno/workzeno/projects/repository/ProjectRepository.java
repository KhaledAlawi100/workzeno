
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


    @Query(
            value = """
        SELECT p.*
        FROM projects p
        WHERE (
            p.owner_id = :userId
            OR EXISTS (
                SELECT 1
                FROM project_memberships pm
                WHERE pm.project_id = p.id
                  AND pm.user_id = :userId
            )
        )
        AND (
            :search = ''
            OR LOWER(p.name) LIKE CONCAT('%', LOWER(:search), '%')
            OR LOWER(COALESCE(p.description, ''))
                LIKE CONCAT('%', LOWER(:search), '%')
        )
        ORDER BY p.created_at DESC, p.id DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM projects p
        WHERE (
            p.owner_id = :userId
            OR EXISTS (
                SELECT 1
                FROM project_memberships pm
                WHERE pm.project_id = p.id
                  AND pm.user_id = :userId
            )
        )
        AND (
            :search = ''
            OR LOWER(p.name) LIKE CONCAT('%', LOWER(:search), '%')
            OR LOWER(COALESCE(p.description, ''))
                LIKE CONCAT('%', LOWER(:search), '%')
        )
        """,
            nativeQuery = true
    )
    Page<Project> findAccessibleProjects(
            @Param("userId") UUID userId,
            @Param("search") String search,
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
