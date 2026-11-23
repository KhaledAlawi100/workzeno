package com.workzeno.workzeno.tasks.dto;

import com.workzeno.workzeno.tasks.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request used when changing only a task's workflow status.
 *
 * Keeping this separate from UpdateTaskRequest allows the frontend
 * to move a task between Kanban columns without sending every field.
 */
public record UpdateTaskStatusRequest(

        @NotNull(message = "Task status is required")
        TaskStatus status

) {
}