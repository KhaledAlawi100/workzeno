package com.workzeno.workzeno.collaboration.invitation.dto;

import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitationStatus;
import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectInvitationSummaryResponse(

        UUID invitationId,

        String inviteeEmail,

        ProjectMembershipRole role,

        ProjectInvitationStatus status,

        String invitedByEmail,

        Instant createdAt,

        Instant expiresAt

) {
}