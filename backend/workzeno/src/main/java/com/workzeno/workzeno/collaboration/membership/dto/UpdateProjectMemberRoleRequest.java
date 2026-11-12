package com.workzeno.workzeno.collaboration.membership.dto;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectMemberRoleRequest(

        @NotNull(message = "Membership role is required")
        ProjectMembershipRole role

) {
}
