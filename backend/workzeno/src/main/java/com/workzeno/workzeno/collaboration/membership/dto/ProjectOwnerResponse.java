
package com.workzeno.workzeno.collaboration.membership.dto;

import java.util.UUID;

public record ProjectOwnerResponse(
        UUID userId,
        String email
) {
}
