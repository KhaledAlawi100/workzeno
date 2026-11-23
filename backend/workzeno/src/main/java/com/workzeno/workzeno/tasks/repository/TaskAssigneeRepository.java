package com.workzeno.workzeno.tasks.repository;

import com.workzeno.workzeno.tasks.domain.TaskAssignee;
import com.workzeno.workzeno.tasks.domain.TaskAssigneeId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Database operations for task assignments.
 */
public interface TaskAssigneeRepository
        extends JpaRepository<TaskAssignee, TaskAssigneeId> {

    /**
     * Load assignments for many tasks in one query.
     *
     * This projection returns only the information needed by the API.
     * It does not load complete Task, ProjectMembership, or User entities.
     */
    @Query("""
            SELECT
                assignment.taskId AS taskId,
                assignment.userId AS userId,
                user.email AS email
            FROM TaskAssignee assignment
            JOIN assignment.membership membership
            JOIN membership.user user
            WHERE assignment.taskId IN :taskIds
            ORDER BY user.email ASC
            """)
    List<TaskAssigneeView> findAssigneesByTaskIds(
            @Param("taskIds") Collection<UUID> taskIds
    );

    /**
     * Delete all assignments for one member in one project.
     *
     * Must be called before deleting the ProjectMembership.
     *
     * clearAutomatically is deliberately false: we do not want this
     * bulk delete to detach every managed entity in the persistence context.
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            DELETE FROM TaskAssignee assignment
            WHERE assignment.projectId = :projectId
              AND assignment.userId = :userId
            """)
    int deleteAllByProjectIdAndUserId(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId
    );

    /**
     * Check whether a user is already assigned to a task.
     *
     * The composite primary key also guarantees uniqueness in the database.
     */
    boolean existsByTaskIdAndUserId(UUID taskId, UUID userId);

    /**
     * Remove one specific user's assignment from one specific task.
     */
    @Modifying
    @Query("""
        DELETE FROM TaskAssignee assignment
        WHERE assignment.taskId = :taskId
          AND assignment.userId = :userId
        """)
    int deleteByTaskIdAndUserId(
            @Param("taskId") UUID taskId,
            @Param("userId") UUID userId
    );


}