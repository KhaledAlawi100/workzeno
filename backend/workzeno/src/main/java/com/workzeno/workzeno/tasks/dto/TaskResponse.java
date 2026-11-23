package com.workzeno.workzeno.tasks.dto;

import com.workzeno.workzeno.tasks.domain.TaskPriority;
import com.workzeno.workzeno.tasks.domain.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskResponse(

        UUID id,

        UUID projectId,

        List<TaskAssigneeResponse> assignees,

        String title,

        String description,

        TaskStatus status,

        TaskPriority priority,

        LocalDate dueDate,

        Instant createdAt,

        Instant updatedAt

) {
}