package com.workzeno.workzeno.dashboard.repository;

import com.workzeno.workzeno.tasks.domain.TaskPriority;
import com.workzeno.workzeno.tasks.domain.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface DashboardTaskSummaryView {

    UUID getId();

    UUID getProjectId();

    String getTitle();

    String getDescription();

    TaskStatus getStatus();

    TaskPriority getPriority();

    LocalDate getDueDate();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}