package com.workzeno.workzeno.projects.dto;

import com.workzeno.workzeno.projects.domain.ProjectStatus;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(

        UUID id,

        String name,

        String description,

        UUID ownerId,

        ProjectStatus status,

        Instant createdAt,

        Instant updatedAt

) {
}