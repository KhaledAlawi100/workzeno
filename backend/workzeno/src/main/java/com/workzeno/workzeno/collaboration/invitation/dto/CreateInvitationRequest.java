
package com.workzeno.workzeno.collaboration.invitation.dto;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInvitationRequest(

        @NotBlank(message = "Invitee email is required")
        @Email(message = "A valid email address is required")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String inviteeEmail,

        @NotNull(message = "Invitation role is required")
        ProjectMembershipRole role

) {
}
