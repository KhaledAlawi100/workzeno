package com.workzeno.workzeno.collaboration.membership.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID userId,
        String email,
        String role,
        Instant joinedAt
) {
}