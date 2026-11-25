
package com.workzeno.workzeno.dashboard.repository;

import com.workzeno.workzeno.projects.domain.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DashboardRepository extends Repository<Project, UUID> {

    /**
     * Counts all projects accessible to the current user,
     * including projects they own and projects they belong to.
     */
    @Query(value = """
            SELECT
                COUNT(*) AS total,
                COUNT(*) FILTER (
                    WHERE p.status = 'ACTIVE'
                ) AS active,
                COUNT(*) FILTER (
                    WHERE p.status = 'ARCHIVED'
                ) AS archived
            FROM projects p
            WHERE
                p.owner_id = :userId
                OR EXISTS (
                    SELECT 1
                    FROM project_memberships pm
                    WHERE pm.project_id = p.id
                      AND pm.user_id = :userId
                )
            """, nativeQuery = true)
    DashboardProjectMetricsView getProjectMetrics(
            @Param("userId") UUID userId
    );


    /**
     * Counts tasks in all projects accessible to the current user.
     *
     * Includes tasks in archived projects.
     * A task is overdue when its due date is before today
     * and its status is not DONE.
     */
    @Query(value = """
            SELECT
                COUNT(*) AS total,

                COUNT(*) FILTER (
                    WHERE t.status = 'TODO'
                ) AS todo,

                COUNT(*) FILTER (
                    WHERE t.status = 'IN_PROGRESS'
                ) AS "inProgress",

                COUNT(*) FILTER (
                    WHERE t.status = 'IN_REVIEW'
                ) AS "inReview",

                COUNT(*) FILTER (
                    WHERE t.status = 'DONE'
                ) AS done,

                COUNT(*) FILTER (
                    WHERE t.due_date < :today
                      AND t.status <> 'DONE'
                ) AS overdue

            FROM tasks t
            JOIN projects p
                ON p.id = t.project_id

            WHERE
                p.owner_id = :userId
                OR EXISTS (
                    SELECT 1
                    FROM project_memberships pm
                    WHERE pm.project_id = p.id
                      AND pm.user_id = :userId
                )
            """, nativeQuery = true)
    DashboardTaskMetricsView getTaskMetrics(
            @Param("userId") UUID userId,
            @Param("today") LocalDate today
    );


    /**
     * Counts all tasks assigned to the current user
     * within projects accessible to that user.
     *
     * Includes completed and incomplete tasks.
     */
    @Query(value = """
            SELECT COUNT(DISTINCT t.id)

            FROM tasks t
            JOIN projects p
                ON p.id = t.project_id

            JOIN task_assignees ta
                ON ta.task_id = t.id
               AND ta.project_id = t.project_id

            WHERE ta.user_id = :userId
              AND (
                    p.owner_id = :userId
                    OR EXISTS (
                        SELECT 1
                        FROM project_memberships pm
                        WHERE pm.project_id = p.id
                          AND pm.user_id = :userId
                    )
              )
            """, nativeQuery = true)
    long countTasksAssignedToUser(
            @Param("userId") UUID userId
    );


    /**
     * Retrieves up to the requested number of open tasks
     * assigned to the current user.
     *
     * Tasks are ordered by:
     * 1. Due date ascending, null dates last.
     * 2. Creation date descending.
     * 3. Task ID ascending.
     */
    @Query(value = """
            SELECT
                t.id AS id,
                t.project_id AS "projectId",
                t.title AS title,
                t.description AS description,
                t.status AS status,
                t.priority AS priority,
                t.due_date AS "dueDate",
                t.created_at AS "createdAt",
                t.updated_at AS "updatedAt"

            FROM tasks t
            JOIN projects p
                ON p.id = t.project_id

            JOIN task_assignees ta
                ON ta.task_id = t.id
               AND ta.project_id = t.project_id

            WHERE ta.user_id = :userId
              AND t.status <> 'DONE'
              AND (
                    p.owner_id = :userId
                    OR EXISTS (
                        SELECT 1
                        FROM project_memberships pm
                        WHERE pm.project_id = p.id
                          AND pm.user_id = :userId
                    )
              )

            ORDER BY
                t.due_date ASC NULLS LAST,
                t.created_at DESC,
                t.id ASC
            """, nativeQuery = true)
    List<DashboardTaskSummaryView> findMyOpenTasks(
            @Param("userId") UUID userId,
            Pageable pageable
    );

}
