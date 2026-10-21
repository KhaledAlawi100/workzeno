
package com.workzeno.workzeno.identity.auth;



import com.workzeno.workzeno.identity.auth.dto.AuthResponse;
import com.workzeno.workzeno.identity.auth.dto.LoginRequest;
import com.workzeno.workzeno.identity.auth.dto.LoginResponse;
import com.workzeno.workzeno.identity.auth.dto.RefreshRequest;


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


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class AuthControllerTest {


    private static final String EMAIL             = "user@example.com";
    private static final String ROLE              = "USER";
    private static final String ACCESS_TOKEN      = "test-access-token";
    private static final String OLD_REFRESH_TOKEN = "old-refresh-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";
    private static final String FRONTEND_ORIGIN   = "http://localhost:5173";


    @Mock
    private AuthService authService;         // Domain logic behind the controller

    @Mock
    private AuthOriginValidator authOriginValidator; // CSRF-prevention guard


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
                new AuthResponseMapper(),
                authOriginValidator
        );


        httpRequest = new MockHttpServletRequest();
        httpRequest.setRequestURI("/api/v1/auth");        // used for path-based checks
        httpRequest.addHeader("Origin", FRONTEND_ORIGIN); // used for CSRF validation
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 1 — LOGIN
    // ═══════════════════════════════════════════════════════════════════
    // Verifies:
    //   ✔ 200 OK
    //   ✔ Response body contains email / role / access token
    //   ✔ Refresh token is NOT in the body (only in the HTTP-only cookie)
    //   ✔ Set-Cookie header has the expected attributes
    //   ✔ Service was invoked exactly once
    // ═══════════════════════════════════════════════════════════════════
    @Test
    void login_shouldReturnAccessTokenAndSetHttpOnlyRefreshCookie() {

        LoginRequest request = new LoginRequest(EMAIL, "StrongPassword123!");

        when(authService.login(request)).thenReturn(
                loginResponse(OLD_REFRESH_TOKEN)
        );


        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


        AuthResponse body = response.getBody().data();

        assertThat(body.email()).isEqualTo(EMAIL);
        assertThat(body.role()).isEqualTo(ROLE);
        assertThat(body.accessToken()).isEqualTo(ACCESS_TOKEN);

        assertThat(body).hasNoNullFieldsOrProperties();


        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken=" + OLD_REFRESH_TOKEN);
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth");
        assertThat(setCookie).contains("SameSite=Lax");
        assertThat(setCookie).doesNotContain("Secure");


        verify(authService).login(request);
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 2 — REFRESH (happy path)
    // ═══════════════════════════════════════════════════════════════════
    // Verifies:
    //   ✔ Service is called with a RefreshRequest wrapping the OLD token
    //   ✔ Response body contains a NEW access token
    //   ✔ Set-Cookie header replaces the cookie with a NEW refresh token
    //   ✔ Origin validator was invoked (CSRF check)
    // ═══════════════════════════════════════════════════════════════════
    @Test
    void refresh_shouldReadCookieTokenAndSetReplacementCookie() {

        when(authService.refresh(new RefreshRequest(OLD_REFRESH_TOKEN)))
                .thenReturn(loginResponse(NEW_REFRESH_TOKEN));


        ResponseEntity<ApiResponse<AuthResponse>> response =
                authController.refresh(OLD_REFRESH_TOKEN, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().accessToken())
                .isEqualTo(ACCESS_TOKEN);

        String setCookie = response.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken=" + NEW_REFRESH_TOKEN);
        assertThat(setCookie).contains("HttpOnly");

        verify(authOriginValidator).validate(httpRequest);
        verify(authService).refresh(new RefreshRequest(OLD_REFRESH_TOKEN));
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 3 — REFRESH without cookie
    // ═══════════════════════════════════════════════════════════════════
    // Verifies:
    //   ✔ Throws ApiException
    //   ✔ ApiException carries 401 UNAUTHORIZED
    //   ✔ Message is exactly "Refresh token is missing"
    //   ✔ Service was NEVER touched (fail fast before business logic)
    //   ✔ Origin validator STILL runs (origin check precedes token check)
    // ═══════════════════════════════════════════════════════════════════
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


        verify(authOriginValidator).validate(httpRequest);


        verifyNoInteractions(authService);
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 4 — REFRESH with blank (whitespace) cookie
    // ───────────────────────────────────────────────────────────────────
    // Verifies:
    //   ✔ Blank/whitespace tokens are treated as missing (input hygiene)
    //   ✔ Same 401 ApiException is thrown
    //   ✔ Service never touched
    //   (Note: origin validator NOT verified here — different path
    //    priority; still fine because we only care about the rejection.)
    // ═══════════════════════════════════════════════════════════════════
    @Test
    void refresh_withBlankCookie_shouldThrowUnauthorizedApiException() {

        assertThatThrownBy(() ->
                authController.refresh("   ", httpRequest)   // whitespace token
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;

                    assertThat(apiException.getStatus())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verifyNoInteractions(authService);
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 5 — LOGOUT (happy path, token present)
    // ───────────────────────────────────────────────────────────────────
    // Verifies:
    //   ✔ 200 OK
    //   ✔ ApiResponse envelope: success=true, message="Logout successful"
    //   ✔ Set-Cookie header CLEARS the cookie (Max-Age=0)
    //   ✔ Service was called to revoke the session server-side
    //   ✔ Origin validator was invoked (CSRF guard)
    // ═══════════════════════════════════════════════════════════════════
    @Test
    void logout_shouldRevokeSessionAndClearCookie() {


        ResponseEntity<ApiResponse<Void>> response =
                authController.logout(OLD_REFRESH_TOKEN, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message())
                .isEqualTo("Logout successful");


        String setCookie = response.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("refreshToken=");
        assertThat(setCookie).contains("Max-Age=0");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth");

        verify(authOriginValidator).validate(httpRequest);
        verify(authService).logout(new RefreshRequest(OLD_REFRESH_TOKEN));
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST 6 — LOGOUT without cookie
    // ───────────────────────────────────────────────────────────────────
    // Verifies:
    //   ✔ Idempotent: logout succeeds even if already logged out
    //   ✔ Cookie is STILL cleared (so the browser state is cleaned up)
    //   ✔ Service.logout is NEVER called (nothing to revoke)
    // ═══════════════════════════════════════════════════════════════════
    @Test
    void logout_withoutCookie_shouldStillClearCookie() {

        ResponseEntity<ApiResponse<Void>> response =
                authController.logout(null, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String setCookie = response.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("Max-Age=0");   // delete anyway

        verify(authOriginValidator).validate(httpRequest);


        verify(authService, never()).logout(any(RefreshRequest.class));
    }


    private LoginResponse loginResponse(String refreshToken) {
        return new LoginResponse(
                EMAIL,
                ROLE,
                ACCESS_TOKEN,
                refreshToken,
                "Bearer",
                900L
        );
    }
}