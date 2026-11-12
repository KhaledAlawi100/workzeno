package com.workzeno.workzeno.collaboration.membership.dto;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProjectMemberRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Membership role is required")
        ProjectMembershipRole role

) {
}