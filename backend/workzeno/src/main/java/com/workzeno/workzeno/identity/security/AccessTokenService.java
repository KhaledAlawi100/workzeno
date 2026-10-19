
        package com.workzeno.workzeno.identity.security;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    // UTC avoids dependence on the server's configured local time zone.
    private final Clock clock = Clock.systemUTC();

    public String issueAccessToken(UUID userId, String role) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = calculateExpiration(issuedAt);

        JwtClaimsSet claims = buildClaims(userId, role, issuedAt, expiresAt);
        JwsHeader header = buildHeader();

        String token = encodeToken(header, claims);

        log.debug("Access token issued successfully");

        return token;
    }

    private Instant calculateExpiration(Instant issuedAt) {
        return issuedAt.plus(jwtProperties.getAccessTokenTtl());
    }

    private JwtClaimsSet buildClaims(
            UUID userId,
            String role,
            Instant issuedAt,
            Instant expiresAt
    ) {
        // The subject identifies the user; do not put sensitive user data
        // such as passwords or other credentials in JWT claims.
        return JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
    }

    private JwsHeader buildHeader() {
        // HS256 signs the token with a shared secret. The encoder and decoder
        // must use compatible configuration and securely managed key material.
        return JwsHeader.with(MacAlgorithm.HS256).build();
    }

    private String encodeToken(
            JwsHeader header,
            JwtClaimsSet claims
    ) {
        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}