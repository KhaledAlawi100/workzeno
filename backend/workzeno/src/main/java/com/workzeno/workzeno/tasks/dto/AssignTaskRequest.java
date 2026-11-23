package com.workzeno.workzeno.tasks.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to assign an existing project member to a task.
 */
public record AssignTaskRequest(

        @NotBlank(message = "Assignee email is required")
        @Email(message = "Assignee email must be valid")
        @Size(max = 254, message = "Assignee email is too long")
        String email

) {
}