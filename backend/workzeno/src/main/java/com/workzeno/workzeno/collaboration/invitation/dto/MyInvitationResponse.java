
package com.workzeno.workzeno.collaboration.invitation.dto;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;

import java.time.Instant;
import java.util.UUID;

public record MyInvitationResponse(

        UUID invitationId,

        UUID projectId,

        String projectName,

        String invitedByEmail,

        ProjectMembershipRole role,

        Instant createdAt,

        Instant expiresAt

) {
}
