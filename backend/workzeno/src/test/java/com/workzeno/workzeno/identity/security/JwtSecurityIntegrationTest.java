package com.workzeno.workzeno.identity.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityIntegrationTest {

    private static final String PROTECTED_URL = "/api/v1/test/protected";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void requestWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenReturns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenAuthenticatesUser() throws Exception {
        String userId = "day5-test-user";
        String token = createToken(userId, Instant.now().plusSeconds(300));

        mockMvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        Instant now = Instant.now();

        String token = createToken(
                "expired-test-user",
                now.minusSeconds(120),
                now.minusSeconds(300)
        );

        mockMvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithDifferentKeyReturns401() throws Exception {
        byte[] wrongKeyBytes = new byte[32];
        java.util.Arrays.fill(wrongKeyBytes, (byte) 0x42);

        SecretKey wrongKey = new SecretKeySpec(wrongKeyBytes, "HmacSHA256");

        JwtEncoder wrongEncoder = new NimbusJwtEncoder(
                new ImmutableSecret<SecurityContext>(wrongKey)
        );

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject("wrong-signature-test-user")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();

        String token = wrongEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();

        mockMvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private String createToken(String subject, Instant expiresAt) {
        return createToken(subject, expiresAt, Instant.now().minusSeconds(1));
    }

    private String createToken(String subject, Instant expiresAt, Instant issuedAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();
    }
}