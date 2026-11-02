package com.workzeno.workzeno.identity.auth.dto.response;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String role,
        String accessToken,
        String tokenType,
        long expiresIn
) {}