package com.workzeno.workzeno.collaboration.dto.invitation;


import com.workzeno.workzeno.collaboration.domain.ProjectMembershipRole;

import java.time.Instant;
import java.util.UUID;

public record AcceptInvitationResponse(
        UUID invitationId,
        UUID projectId,
        String projectName,
        ProjectMembershipRole role,
        Instant acceptedAt
) {}
