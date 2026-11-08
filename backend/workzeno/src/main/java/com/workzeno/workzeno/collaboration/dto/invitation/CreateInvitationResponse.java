
package com.workzeno.workzeno.collaboration.dto.invitation;

import com.workzeno.workzeno.collaboration.domain.ProjectMembershipRole;

import java.time.Instant;
import java.util.UUID;

public record CreateInvitationResponse(

        UUID invitationId,

        String inviteeEmail,

        ProjectMembershipRole role,

        Instant expiresAt,

        String token

) {
}
