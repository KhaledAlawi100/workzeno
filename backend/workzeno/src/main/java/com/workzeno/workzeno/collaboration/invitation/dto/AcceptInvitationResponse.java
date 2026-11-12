package com.workzeno.workzeno.collaboration.invitation.dto;


import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;

import java.time.Instant;
import java.util.UUID;

public record AcceptInvitationResponse(
        UUID invitationId,
        UUID projectId,
        String projectName,
        ProjectMembershipRole role,
        Instant acceptedAt
) {}
