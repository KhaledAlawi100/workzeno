
package com.workzeno.workzeno.identity.auth;

import java.util.Locale;

import com.workzeno.workzeno.identity.auth.dto.LoginRequest;
import com.workzeno.workzeno.identity.auth.dto.LoginResponse;
import com.workzeno.workzeno.identity.auth.dto.RegisterRequest;
import com.workzeno.workzeno.identity.auth.dto.RegisterResponse;
import com.workzeno.workzeno.identity.security.AccessTokenService;
import com.workzeno.workzeno.identity.security.JwtProperties;
import com.workzeno.workzeno.identity.user.User;
import com.workzeno.workzeno.identity.user.UserRepository;
import com.workzeno.workzeno.identity.user.WorkzenoUserDetails;
import com.workzeno.workzeno.shared.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokenService;
    private final JwtProperties jwtProperties;

    // Registration

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(email, passwordHash);

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getRole().name(),
                    savedUser.getStatus().name(),
                    savedUser.getCreatedAt()
            );
        } catch (DataIntegrityViolationException exception) {
            // The database uniqueness constraint remains the final safeguard
            // against concurrent duplicate registrations.
            throw new DuplicateEmailException();
        }
    }

    // Login

    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        final Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            email,
                            request.password()
                    )
            );
        } catch (AuthenticationException exception) {
            // Do not log credentials or expose whether the email exists.
            log.warn("Authentication failed");
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication failed",
                    "Invalid email or password"
            );
        }

        if (authentication == null) {
            log.error("Authentication manager returned no authentication");
            throw new IllegalStateException(
                    "Authentication manager returned no authentication"
            );
        }

        if (!(authentication.getPrincipal()
                instanceof WorkzenoUserDetails userDetails)) {
            log.error("Authentication returned an unexpected principal type");
            throw new IllegalStateException(
                    "Unexpected authenticated principal type"
            );
        }

        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user has no role authority"
                ));

        // Never issue an access token before authentication succeeds.
        String accessToken =
                accessTokenService.issueAccessToken(userDetails.getUserId());

        log.info("Authentication succeeded");

        return new LoginResponse(
                userDetails.getUsername(),
                role,
                accessToken,
                "Bearer",
                jwtProperties.getAccessTokenTtl().toSeconds()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
