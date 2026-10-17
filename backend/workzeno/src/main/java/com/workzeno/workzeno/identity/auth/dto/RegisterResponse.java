package com.workzeno.workzeno.identity.auth.dto;

import java.time.Instant;
import java.util.UUID;
public record RegisterResponse(
        UUID id,
        String email,
        String role,
        String status,
        Instant createdAt
) {
}

