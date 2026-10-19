package com.workzeno.workzeno.identity.auth;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import com.workzeno.workzeno.identity.auth.dto.LoginRequest;
import com.workzeno.workzeno.identity.auth.dto.LoginResponse;
import com.workzeno.workzeno.identity.auth.dto.RefreshRequest;
import com.workzeno.workzeno.identity.auth.dto.RegisterRequest;
import com.workzeno.workzeno.identity.auth.dto.RegisterResponse;
import com.workzeno.workzeno.identity.security.AccessTokenService;
import com.workzeno.workzeno.identity.security.JwtProperties;
import com.workzeno.workzeno.identity.security.RefreshTokenProperties;
import com.workzeno.workzeno.identity.security.RefreshTokenService;
import com.workzeno.workzeno.identity.user.User;
import com.workzeno.workzeno.identity.user.UserRepository;
import com.workzeno.workzeno.identity.user.UserStatus;
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
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenProperties refreshTokenProperties;
    private final RefreshSessionRepository refreshSessionRepository;
    private final RefreshSessionRevocationService refreshSessionRevocationService;

    // ============================================================
    // Registration
    // ============================================================

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email);

        User user = new User(
                email,
                passwordEncoder.encode(request.password())
        );

        return saveRegisteredUser(user);
    }

    private void ensureEmailAvailable(String email) {
        // Two concurrent registrations can both pass this check.
        // The real guarantee is the unique constraint on the email column.
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
    }

    private RegisterResponse saveRegisteredUser(User user) {
        try {
            // saveAndFlush forces the INSERT to hit the DB inside this try,
            // so a unique-constraint violation surfaces here rather than at
            // transaction commit, where it would escape this catch block.
            User savedUser = userRepository.saveAndFlush(user);
            return toRegisterResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    private RegisterResponse toRegisterResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }

    // ============================================================
    // Login: authenticate, issue tokens, and persist a refresh session.
    // ============================================================

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Authentication authentication = authenticate(email, request.password());

        WorkzenoUserDetails userDetails = extractUserDetails(authentication);
        String role = extractRole(authentication);
        Instant now = Instant.now();

        // Include the authenticated user's role in the access token so
        // Spring Security can apply role-based authorization.
        String accessToken = accessTokenService.issueAccessToken(
                userDetails.getUserId(),
                role
        );

        String refreshToken = refreshTokenService.generateToken();

        // the ( raw refresh token ) is returned to the client and (( NEVER ) stored.
        // Only its SHA-256 hash is persisted
        RefreshSession session = newRefreshSession(
                userDetails.getUserId(),
                UUID.randomUUID(), // One new family per login/device.
                refreshToken,
                now
        );

        refreshSessionRepository.save(session);

        log.info("Authentication succeeded");

        return buildLoginResponse(
                userDetails.getUsername(),
                role,
                accessToken,
                refreshToken
        );
    }

    private Authentication authenticate(String email, String password) {
        try {
            // Delegates to UserDetailsService + password check + status check
            return authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, password)
            );
        } catch (AuthenticationException exception) {
            log.warn("Authentication failed");
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication failed",
                    "Invalid email or password"
            );
        }
    }

    private WorkzenoUserDetails extractUserDetails(
            Authentication authentication
    ) {
        if (!(authentication.getPrincipal()
                instanceof WorkzenoUserDetails userDetails)) {
            throw new IllegalStateException(
                    "Unexpected authenticated principal type"
            );
        }

        return userDetails;
    }

    private String extractRole(Authentication authentication) {
        // Spring authorities are prefixed with "ROLE_"; strip the prefix
        // to expose the raw role name in API responses.
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user has no role authority"
                ));
    }

    // ============================================================
    // Refresh: exchange a refresh token for a new token pair.
    // ============================================================

    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        String submittedToken = request.refreshToken();
        String tokenHash = refreshTokenService.hashToken(submittedToken);

        RefreshSession oldSession = findRefreshSessionForUpdate(tokenHash);
        Instant now = Instant.now();

        validateRefreshSession(oldSession, now);
        User user = findActiveUser(oldSession.getUserId());

        String newRefreshToken = refreshTokenService.generateToken();

        RefreshSession replacement = newRefreshSession(
                oldSession.getUserId(),
                oldSession.getFamilyId(),
                newRefreshToken,
                now
        );

        rotateRefreshSession(oldSession, replacement, now);

        // Re-issue the access token using the user's current database role,
        // so role changes since login are reflected in the new JWT.
        String newAccessToken = accessTokenService.issueAccessToken(
                user.getId(),
                user.getRole().name()
        );

        return buildLoginResponse(
                user.getEmail(),
                user.getRole().name(),
                newAccessToken,
                newRefreshToken
        );
    }

    private RefreshSession findRefreshSessionForUpdate(String tokenHash) {
        return refreshSessionRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(this::invalidRefreshToken);
    }

    private void validateRefreshSession(
            RefreshSession session,
            Instant now
    ) {
        // SECURITY: reuse of a revoked token can indicate token theft.
        // Expiry is strict: expires_at == now is treated as expired.
        if (session.getRevokedAt() != null) {
            // A revoked token with a replacement was already rotated.
            // Reuse can indicate token theft: revoke the remaining family.
            if (session.getReplacedById() != null) {
                // This service commits revocation independently, before
                // invalidRefreshToken() is thrown and the outer transaction rolls back.
                refreshSessionRevocationService.revokeFamily(
                        session.getFamilyId(),
                        now
                );
            }

            throw invalidRefreshToken();
        }

        if (!session.getExpiresAt().isAfter(now)) {
            throw invalidRefreshToken();
        }
    }

    private User findActiveUser(UUID userId) {
        // Re-check status on every refresh so a disabled user cannot obtain
        // a new token pair using a session issued before the account was disabled.
        return userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(this::invalidRefreshToken);
    }

    private RefreshSession newRefreshSession(
            UUID userId,
            UUID familyId,
            String rawToken,
            Instant now
    ) {
        RefreshSession session = new RefreshSession();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setFamilyId(familyId);

        // Persist only the token hash, never the raw refresh token.
        // The lowercase hexadecimal SHA-256 hash matches the DB CHECK
        // constraint and the unique index used for lookup.
        session.setTokenHash(refreshTokenService.hashToken(rawToken));

        session.setCreatedAt(now);
        // Per-row expiry: rotation extends the chain, while dormant tokens expire.
        session.setExpiresAt(now.plus(refreshTokenProperties.ttl()));

        return session;
    }

    private void rotateRefreshSession(
            RefreshSession oldSession,
            RefreshSession replacement,
            Instant now
    ) {
        // SAME family_id: rotation preserves the chain of custody so the
        // entire device/browser session family can be revoked as a unit.

        // ORDERING MATTERS: insert and flush the replacement BEFORE setting
        // replaced_by_id on the old row. That column is an FK to refresh_sessions(id).
        refreshSessionRepository.saveAndFlush(replacement);

        // Atomically retire the old token and link it to its successor.
        // (revoked_at, replaced_by_id) = (not null, not null) means "rotated".
        oldSession.setRevokedAt(now);
        oldSession.setReplacedById(replacement.getId());
        refreshSessionRepository.save(oldSession);
    }

    private LoginResponse buildLoginResponse(
            String username,
            String role,
            String accessToken,
            String refreshToken
    ) {
        return new LoginResponse(
                username,
                role,
                accessToken,
                refreshToken,
                "Bearer",
                jwtProperties.getAccessTokenTtl().toSeconds()
        );
    }

    // One identical error for every refresh failure mode: missing row, expired
    // token, revoked token, or disabled user. Prevents callers from distinguishing
    // a nonexistent token from a revoked one.
    private ApiException invalidRefreshToken() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "Invalid refresh token",
                "The refresh token is invalid, expired, or revoked"
        );
    }

    // ============================================================
    // Logout: revoke the refresh session associated with this token.
    // ============================================================
    @Transactional
    public void logout(RefreshRequest request) {
        String tokenHash = refreshTokenService.hashToken(
                request.refreshToken()
        );

        refreshSessionRepository
                .findByTokenHashForUpdate(tokenHash)
                .ifPresent(session -> {
                    if (session.getRevokedAt() == null) {
                        session.setRevokedAt(Instant.now());
                        refreshSessionRepository.save(session);
                        log.info("Refresh session revoked");
                    }
                });
    }

    // Locale.ROOT is NOT optional. "I".toLowerCase() under a Turkish locale
    // yields "ı" (dotless i), which could create a second account for the same
    // email. Trimming and lowercasing here define the canonical email.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}