package com.workzeno.workzeno.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "Project name is required")
        @Size(max = 120, message = "Project name must not exceed 120 characters")
        String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description

) {
}