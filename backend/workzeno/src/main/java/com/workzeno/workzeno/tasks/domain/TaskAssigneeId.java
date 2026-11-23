package com.workzeno.workzeno.tasks.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for TaskAssignee.
 *
 * It matches the database primary key:
 * PRIMARY KEY (task_id, user_id)
 */
public class TaskAssigneeId implements Serializable {

    private UUID taskId;

    private UUID userId;

    // Required by JPA.
    public TaskAssigneeId() {
    }

    public TaskAssigneeId(UUID taskId, UUID userId) {
        this.taskId = taskId;
        this.userId = userId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof TaskAssigneeId that)) {
            return false;
        }

        return Objects.equals(taskId, that.taskId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userId);
    }
}