
package com.workzeno.workzeno.identity.auth;

import com.workzeno.workzeno.identity.auth.dto.LoginRequest;
import com.workzeno.workzeno.identity.auth.dto.LoginResponse;
import com.workzeno.workzeno.identity.auth.dto.RegisterRequest;
import com.workzeno.workzeno.identity.auth.dto.RegisterResponse;
import com.workzeno.workzeno.identity.security.AccessTokenService;
import com.workzeno.workzeno.identity.security.JwtProperties;
import com.workzeno.workzeno.identity.security.RefreshTokenProperties;
import com.workzeno.workzeno.identity.security.RefreshTokenService;
import com.workzeno.workzeno.identity.user.User;
import com.workzeno.workzeno.identity.user.UserRepository;
import com.workzeno.workzeno.identity.user.UserRole;
import com.workzeno.workzeno.identity.user.UserStatus;
import com.workzeno.workzeno.identity.user.WorkzenoUserDetails;
import com.workzeno.workzeno.shared.exception.ApiException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenProperties refreshTokenProperties;

    @Mock
    private RefreshSessionRepository refreshSessionRepository;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "khaled@example.com";
    private static final String PASSWORD = "StrongPassword123!";
    private static final String HASH = "$2a$12$exampleHash";
    private static final String TOKEN = "signed.jwt.token";
    private static final String REFRESH_TOKEN = "test-refresh-token";
    private static final String REFRESH_TOKEN_HASH =
            "abcdef0123456789abcdef0123456789"
                    + "abcdef0123456789abcdef0123456789";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final UUID USER_ID = UUID.randomUUID();

    // ---------------------------------------------------------
    // Registration tests
    // ---------------------------------------------------------

    @Test
    void register_shouldNormalizeEmailAndHashPassword() {
        RegisterRequest request =
                new RegisterRequest("  KHALED@EXAMPLE.COM  ", PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASH);

        User savedUser = mock(User.class);
        UUID savedUserId = UUID.randomUUID();

        when(savedUser.getId()).thenReturn(savedUserId);
        when(savedUser.getEmail()).thenReturn(EMAIL);
        when(savedUser.getRole()).thenReturn(UserRole.USER);
        when(savedUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(savedUser.getCreatedAt()).thenReturn(Instant.now());

        when(userRepository.saveAndFlush(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).saveAndFlush(userCaptor.capture());
        verify(passwordEncoder).encode(PASSWORD);

        assertThat(userCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(HASH);
        assertThat(response.email()).isEqualTo(EMAIL);
        verify(userRepository).existsByEmail(EMAIL);
    }

    @Test
    void register_shouldRejectExistingEmail() {
        RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_shouldTranslateDatabaseUniqueViolation() {
        RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASH);
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(
                        new DataIntegrityViolationException("unique violation")
                );

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    // ---------------------------------------------------------
    // Login tests
    // ---------------------------------------------------------

    @Test
    void login_shouldAuthenticateAndIssueBothTokens() {
        LoginRequest request =
                new LoginRequest("  KHALED@EXAMPLE.COM  ", PASSWORD);

        when(jwtProperties.getAccessTokenTtl()).thenReturn(TOKEN_TTL);
        when(refreshTokenProperties.ttl())
                .thenReturn(Duration.ofDays(7));

        when(refreshTokenService.generateToken())
                .thenReturn(REFRESH_TOKEN);
        when(refreshTokenService.hashToken(REFRESH_TOKEN))
                .thenReturn(REFRESH_TOKEN_HASH);

        WorkzenoUserDetails userDetails = mock(WorkzenoUserDetails.class);
        when(userDetails.getUserId()).thenReturn(USER_ID);
        when(userDetails.getUsername()).thenReturn(EMAIL);

        Authentication authentication =
                authenticationWith(
                        userDetails,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(accessTokenService.issueAccessToken(USER_ID))
                .thenReturn(TOKEN);

        LoginResponse response = authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(authenticationManager).authenticate(authCaptor.capture());

        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo(EMAIL);
        assertThat(authCaptor.getValue().getCredentials()).isEqualTo(PASSWORD);
        assertThat(authCaptor.getValue().isAuthenticated()).isFalse();

        verify(accessTokenService).issueAccessToken(USER_ID);
        verify(refreshTokenService).generateToken();
        verify(refreshTokenService).hashToken(REFRESH_TOKEN);

        ArgumentCaptor<RefreshSession> sessionCaptor =
                ArgumentCaptor.forClass(RefreshSession.class);

        verify(refreshSessionRepository).save(sessionCaptor.capture());

        RefreshSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getId()).isNotNull();
        assertThat(savedSession.getUserId()).isEqualTo(USER_ID);
        assertThat(savedSession.getFamilyId()).isNotNull();
        assertThat(savedSession.getTokenHash())
                .isEqualTo(REFRESH_TOKEN_HASH);
        assertThat(savedSession.getCreatedAt()).isNotNull();
        assertThat(savedSession.getExpiresAt())
                .isAfter(savedSession.getCreatedAt());

        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.accessToken()).isEqualTo(TOKEN);
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    void login_shouldReturnUnauthorizedForBadCredentials() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verifyNoInteractions(accessTokenService);
        verifyNoInteractions(refreshTokenService);
        verifyNoInteractions(refreshSessionRepository);
    }

    @Test
    void login_shouldRejectUnexpectedPrincipal() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal())
                .thenReturn("unexpected-principal");

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected authenticated principal type");

        verifyNoInteractions(accessTokenService);
        verifyNoInteractions(refreshTokenService);
        verifyNoInteractions(refreshSessionRepository);
    }

    @Test
    void login_shouldRejectAuthenticationWithoutRole() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        WorkzenoUserDetails userDetails = mock(WorkzenoUserDetails.class);

        Authentication authentication =
                authenticationWith(userDetails, List.of());

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authenticated user has no role authority");

        verifyNoInteractions(accessTokenService);
        verifyNoInteractions(refreshTokenService);
        verifyNoInteractions(refreshSessionRepository);
    }

    private Authentication authenticationWith(
            WorkzenoUserDetails userDetails,
            List<SimpleGrantedAuthority> authorities) {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        doReturn(authorities).when(authentication).getAuthorities();
        return authentication;
    }
}
