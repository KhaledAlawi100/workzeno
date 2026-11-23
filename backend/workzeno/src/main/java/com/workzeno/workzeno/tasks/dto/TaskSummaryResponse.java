package com.workzeno.workzeno.tasks.dto;

import com.workzeno.workzeno.tasks.domain.TaskPriority;
import com.workzeno.workzeno.tasks.domain.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


public record TaskSummaryResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt
) {
}