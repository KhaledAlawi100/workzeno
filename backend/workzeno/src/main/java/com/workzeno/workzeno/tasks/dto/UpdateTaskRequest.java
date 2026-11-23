package com.workzeno.workzeno.tasks.dto;

import com.workzeno.workzeno.tasks.domain.TaskPriority;
import com.workzeno.workzeno.tasks.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Task title must not exceed 200 characters")
        String title,

        @Size(max = 2000, message = "Task description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Task priority is required")
        TaskPriority priority,

        @NotNull(message = "Task status is required")
        TaskStatus status,

        LocalDate dueDate

) {
}