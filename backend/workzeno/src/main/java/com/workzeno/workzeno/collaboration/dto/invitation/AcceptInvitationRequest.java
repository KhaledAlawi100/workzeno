package com.workzeno.workzeno.collaboration.dto.invitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(

        @NotBlank(message = "Invitation token is required")
        @Size(max = 128, message = "Invitation token is too long")
        String token

) {
}

