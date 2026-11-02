package com.workzeno.workzeno.identity.auth.service.result;

import com.workzeno.workzeno.identity.auth.dto.response.AuthResponse;

public record AuthenticationResult(
        AuthResponse response,
        String refreshToken
) {}