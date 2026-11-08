
package com.workzeno.workzeno.collaboration.dto.invitation;

import com.workzeno.workzeno.collaboration.domain.ProjectMembershipRole;

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
