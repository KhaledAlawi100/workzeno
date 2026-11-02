package com.workzeno.workzeno.identity.auth;

import com.workzeno.workzeno.identity.auth.controller.AuthController;
import com.workzeno.workzeno.identity.auth.dto.request.LoginRequest;
import com.workzeno.workzeno.identity.auth.dto.request.RefreshRequest;
import com.workzeno.workzeno.identity.auth.dto.response.AuthResponse;
import com.workzeno.workzeno.identity.auth.service.AuthService;
import com.workzeno.workzeno.identity.auth.service.RefreshCookieService;
import com.workzeno.workzeno.identity.auth.service.result.AuthenticationResult;
import com.workzeno.workzeno.identity.auth.validation.AuthOriginValidator;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;
import com.workzeno.workzeno.shared.exception.ApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final UUID USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final String EMAIL = "user@example.com";
    private static final String ROLE = "USER";
    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String OLD_REFRESH_TOKEN = "old-refresh-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";
    private static final String FRONTEND_ORIGIN = "http://localhost:5173";

    @Mock
    private AuthService authService;

    @Mock
    private AuthOriginValidator authOriginValidator;

    private AuthController authController;
    private MockHttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        RefreshCookieService cookieService =
                new RefreshCookieService(Duration.ofDays(7), false);

        authController = new AuthController(
                authService,
                new ApiResponseFactory(),
                cookieService,
                authOriginValidator
        );

        httpRequest = new MockHttpServletRequest();
        httpRequest.setRequestURI("/api/v1/auth");
        httpRequest.addHeader("Origin", FRONTEND_ORIGIN);
    }

    // ================================================================
    // TEST 1 — LOGIN
    // ================================================================

    @Test
    void login_shouldReturnAccessTokenAndSetHttpOnlyRefreshCookie() {

        LoginRequest request =
                new LoginRequest(EMAIL, "StrongPassword123!");

        when(authService.login(request))
                .thenReturn(authenticationResult(OLD_REFRESH_TOKEN));

        ResponseEntity<ApiResponse<AuthResponse>> response =
                authController.login(request, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        AuthResponse body = response.getBody().data();

        assertThat(body.userId()).isEqualTo(USER_ID);
        assertThat(body.email()).isEqualTo(EMAIL);
        assertThat(body.role()).isEqualTo(ROLE);
        assertThat(body.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(body.tokenType()).isEqualTo("Bearer");
        assertThat(body.expiresIn()).isEqualTo(900L);
        assertThat(body).hasNoNullFieldsOrProperties();

        // The raw refresh token must not be part of the JSON response.
        assertThat(body.toString()).doesNotContain(OLD_REFRESH_TOKEN);

        String setCookie =
                response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken=" + OLD_REFRESH_TOKEN);
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth");
        assertThat(setCookie).contains("SameSite=Lax");
        assertThat(setCookie).doesNotContain("Secure");

        verify(authService).login(request);
    }

    // ================================================================
    // TEST 2 — REFRESH (happy path)
    // ================================================================

    @Test
    void refresh_shouldReadCookieTokenAndSetReplacementCookie() {

        when(authService.refresh(new RefreshRequest(OLD_REFRESH_TOKEN)))
                .thenReturn(authenticationResult(NEW_REFRESH_TOKEN));

        ResponseEntity<ApiResponse<AuthResponse>> response =
                authController.refresh(OLD_REFRESH_TOKEN, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        AuthResponse body = response.getBody().data();

        assertThat(body.userId()).isEqualTo(USER_ID);
        assertThat(body.email()).isEqualTo(EMAIL);
        assertThat(body.role()).isEqualTo(ROLE);
        assertThat(body.accessToken()).isEqualTo(ACCESS_TOKEN);

        // The replacement refresh token must not be in the JSON response.
        assertThat(body.toString()).doesNotContain(NEW_REFRESH_TOKEN);

        String setCookie =
                response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken=" + NEW_REFRESH_TOKEN);
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth");
        assertThat(setCookie).contains("SameSite=Lax");

        verify(authOriginValidator).validate(httpRequest);
        verify(authService).refresh(new RefreshRequest(OLD_REFRESH_TOKEN));
    }

    // ================================================================
    // TEST 3 — REFRESH without cookie
    // ================================================================

    @Test
    void refresh_withoutCookie_shouldThrowUnauthorizedApiException() {

        assertThatThrownBy(() ->
                authController.refresh(null, httpRequest)
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);

                    assertThat(apiException.getMessage())
                            .isEqualTo("Refresh token is missing");
                });

        // Origin validation occurs before the missing-token check.
        verify(authOriginValidator).validate(httpRequest);

        verifyNoInteractions(authService);
    }

    // ================================================================
    // TEST 4 — REFRESH with blank cookie
    // ================================================================

    @Test
    void refresh_withBlankCookie_shouldThrowUnauthorizedApiException() {

        assertThatThrownBy(() ->
                authController.refresh("   ", httpRequest)
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);

                    assertThat(apiException.getMessage())
                            .isEqualTo("Refresh token is missing");
                });

        verify(authOriginValidator).validate(httpRequest);
        verifyNoInteractions(authService);
    }

    // ================================================================
    // TEST 5 — LOGOUT (token present)
    // ================================================================

    @Test
    void logout_shouldRevokeSessionAndClearCookie() {

        ResponseEntity<ApiResponse<Void>> response =
                authController.logout(OLD_REFRESH_TOKEN, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message())
                .isEqualTo("Logout successful");

        String setCookie =
                response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken=");
        assertThat(setCookie).contains("Max-Age=0");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth");

        verify(authOriginValidator).validate(httpRequest);
        verify(authService).logout(new RefreshRequest(OLD_REFRESH_TOKEN));
    }

    // ================================================================
    // TEST 6 — LOGOUT without cookie
    // ================================================================

    @Test
    void logout_withoutCookie_shouldStillClearCookie() {

        ResponseEntity<ApiResponse<Void>> response =
                authController.logout(null, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();

        String setCookie =
                response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("Max-Age=0");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth");

        verify(authOriginValidator).validate(httpRequest);

        verify(authService, never()).logout(any(RefreshRequest.class));
    }

    // ================================================================
    // TEST HELPER
    // ================================================================

    private AuthenticationResult authenticationResult(String refreshToken) {

        AuthResponse response = new AuthResponse(
                USER_ID,
                EMAIL,
                ROLE,
                ACCESS_TOKEN,
                "Bearer",
                900L
        );

        return new AuthenticationResult(response, refreshToken);
    }
}