
package com.workzeno.workzeno.identity.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    // SecureRandom is designed for cryptographically strong random values.
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a cryptographically secure refresh token.
     *
     * The raw token is returned to the client only. Persist its hash,
     * not this value, so a database leak does not directly expose usable tokens.
     */
    public String generateToken() {
        byte[] randomBytes = generateRandomBytes();
        return encodeToken(randomBytes);
    }

    /**
     * Creates a deterministic SHA-256 hash of a refresh token.
     *
     * The same token always produces the same hash, allowing the application
     * to look up the session using its unique token_hash database index.
     */
    public String hashToken(String token) {
        validateToken(token);

        byte[] digest = calculateSha256(token);
        return encodeHashAsHex(digest);
    }

    private byte[] generateRandomBytes() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private String encodeToken(byte[] bytes) {
        // URL-safe Base64 avoids characters that require special handling
        // in HTTP headers and request payloads. Padding is unnecessary here.
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private void validateToken(String token) {
        // Reject missing tokens before hashing. Do not trim or normalize
        // tokens: changing the submitted value would change its identity.
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token must not be blank"
            );
        }
    }

    private byte[] calculateSha256(String token) {
        try {
            // SHA-256 is suitable for hashing high-entropy, randomly generated
            // tokens. Unlike passwords, these tokens do not need a slow,
            // salted password-hashing algorithm.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return digest.digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 is required by standard Java implementations.
            // Fail explicitly rather than issuing or accepting an unhashed token.
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }

    private String encodeHashAsHex(byte[] digest) {
        // Lowercase hexadecimal produces a deterministic 64-character hash,
        // matching the refresh_sessions.token_hash database constraint.
        return HexFormat.of().formatHex(digest);
    }
}
