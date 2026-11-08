
package com.workzeno.workzeno.identity.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import com.workzeno.workzeno.shared.security.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final TokenGenerator tokenGenerator;

    public String generateToken() {
        return tokenGenerator.generateToken();
    }


    public String hashToken(String token) {
        return tokenGenerator.hashToken(token);
    }



    private void validateInvitationToken(String token) {
        if (token == null
                || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw new IllegalArgumentException(
                    "Invalid invitation token format"
            );
        }
    }

}
