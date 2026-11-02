package com.workzeno.workzeno.collaboration.dto;

import com.workzeno.workzeno.collaboration.domain.ProjectMembershipRole;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectMemberRoleRequest(

        @NotNull(message = "Membership role is required")
        ProjectMembershipRole role

) {
}
