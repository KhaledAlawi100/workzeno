package com.workzeno.workzeno.projects.dto;

import com.workzeno.workzeno.projects.domain.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record ProjectSummaryResponse(

        UUID id,

        String name,

        String description,

        ProjectStatus status,

        Instant updatedAt

) {
}