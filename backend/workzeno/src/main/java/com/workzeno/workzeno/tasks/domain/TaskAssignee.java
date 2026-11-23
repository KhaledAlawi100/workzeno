package com.workzeno.workzeno.tasks.domain;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembership;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents one assignment between a task and a project member.
 *
 * One Task can have many TaskAssignee records.
 * One ProjectMembership can appear in many TaskAssignee records.
 *
 * The composite primary key matches V10:
 * (task_id, user_id).
 */
@Entity
@Table(name = "task_assignees")
@IdClass(TaskAssigneeId.class)
@Getter
public class TaskAssignee {

    // Part one of the composite primary key.
    @Id
    @Column(name = "task_id", nullable = false, updatable = false)
    private UUID taskId;

    // Part two of the composite primary key.
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    // Needed by the composite foreign key to project_memberships.
    @Column(name = "project_id", nullable = false, updatable = false)
    private UUID projectId;

    // Read-only relationship to the task.
    // The task_id column is written through the taskId field above.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "task_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false
    )
    private Task task;

    // Read-only relationship to the project membership.
    // The project_id and user_id columns are written through scalar fields.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(
                    name = "project_id",
                    referencedColumnName = "project_id",
                    insertable = false,
                    updatable = false
            ),
            @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "user_id",
                    insertable = false,
                    updatable = false
            )
    })
    private ProjectMembership membership;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected TaskAssignee() {
        // Required by JPA.
    }

    /**
     * Creates an assignment after the task has been saved
     * and therefore already has its generated UUID.
     */
    public TaskAssignee(Task task, ProjectMembership membership) {

        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException(
                    "A saved task is required"
            );
        }

        if (membership == null || membership.getUser() == null) {
            throw new IllegalArgumentException(
                    "A project membership with a user is required"
            );
        }

        UUID taskProjectId = task.getProject().getId();
        UUID membershipProjectId = membership.getProject().getId();

        if (!taskProjectId.equals(membershipProjectId)) {
            throw new IllegalArgumentException(
                    "The assignee must belong to the task's project"
            );
        }

        this.task = task;
        this.membership = membership;

        this.taskId = task.getId();
        this.projectId = taskProjectId;
        this.userId = membership.getUser().getId();
    }

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }
}