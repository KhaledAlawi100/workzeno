package com.workzeno.workzeno.collaboration.membership.dto;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;

import java.util.UUID;


public record MyProjectMembershipResponse(
        UUID projectId,
        ProjectMembershipRole role
) {
}
