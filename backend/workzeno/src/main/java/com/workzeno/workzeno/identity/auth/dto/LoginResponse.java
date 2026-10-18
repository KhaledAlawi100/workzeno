
package com.workzeno.workzeno.identity.auth.dto;

public record LoginResponse(
        String email,
        String role,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
