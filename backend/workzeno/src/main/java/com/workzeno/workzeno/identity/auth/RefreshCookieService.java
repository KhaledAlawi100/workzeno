
package com.workzeno.workzeno.identity.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshCookieService {

    public static final String COOKIE_NAME = "refreshToken";

    private static final String COOKIE_PATH = "/api/v1/auth";

    private final Duration refreshTokenTtl;
    private final boolean secure;

    public RefreshCookieService(
            @Value("${app.auth.refresh-token.ttl}") Duration refreshTokenTtl,
            @Value("${app.auth.refresh-token.cookie-secure:false}") boolean secure
    ) {
        this.refreshTokenTtl = refreshTokenTtl;
        this.secure = secure;
    }

    public ResponseCookie create(String refreshToken) {
        return build(refreshToken, refreshTokenTtl);
    }

    public ResponseCookie clear() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String token, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
