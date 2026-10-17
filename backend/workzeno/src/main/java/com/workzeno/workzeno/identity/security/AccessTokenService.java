package com.workzeno.workzeno.identity.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader; // CORRECT IMPORT
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock = Clock.systemUTC();

    public String issueAccessToken(UUID userId) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(
                jwtProperties.getAccessTokenTtl()
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        // JwsHeader now resolves correctly with the updated import
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                // The from() method now receives the correct JwsHeader type
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        log.debug("Access token issued successfully");

        return token;
    }
}