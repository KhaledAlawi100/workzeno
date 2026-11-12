
package com.workzeno.workzeno.collaboration.invitation.dto;

import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitationStatus;
import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Response returned when project owners or admins
 * retrieve invitations created for a project.
 *
 * Includes invitation lifecycle and audit information.
 */
public record ProjectInvitationResponse(

        UUID invitationId,

        String inviteeEmail,

        ProjectMembershipRole role,

        ProjectInvitationStatus status,

        String invitedByEmail,

        Instant createdAt,

        Instant expiresAt,

        Instant acceptedAt,

        Instant declinedAt,

        Instant revokedAt,

        String revokedByEmail

) {
}
