package com.workzeno.workzeno.identity.auth.dto;

public record AuthResponse(
        String email,
        String role,
        String accessToken,
        String tokenType,
        long expiresIn
) {}