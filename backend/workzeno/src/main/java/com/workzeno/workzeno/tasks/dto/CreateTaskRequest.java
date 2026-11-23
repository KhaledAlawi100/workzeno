package com.workzeno.workzeno.tasks.dto;

import com.workzeno.workzeno.tasks.domain.TaskPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateTaskRequest(

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Task title must not exceed 200 characters")
        String title,

        @Size(max = 2000, message = "Task description must not exceed 2000 characters")
        String description,

        TaskPriority priority,

        LocalDate dueDate,

        @Size(max = 50, message = "A task cannot have more than 50 assignees")
        List<
                @NotBlank(message = "Assignee email cannot be blank")
                @Email(message = "Assignee email must be valid")
                @Size(max = 254, message = "Assignee email is too long")
                        String
                > assigneeEmails

) {
}