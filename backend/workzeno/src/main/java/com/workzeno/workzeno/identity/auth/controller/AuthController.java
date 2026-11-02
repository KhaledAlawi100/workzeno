package com.workzeno.workzeno.identity.auth.controller;

import com.workzeno.workzeno.identity.auth.validation.AuthOriginValidator;
import com.workzeno.workzeno.identity.auth.dto.request.LoginRequest;
import com.workzeno.workzeno.identity.auth.dto.request.RefreshRequest;
import com.workzeno.workzeno.identity.auth.dto.request.RegisterRequest;
import com.workzeno.workzeno.identity.auth.dto.response.AuthResponse;
import com.workzeno.workzeno.identity.auth.dto.response.RegisterResponse;
import com.workzeno.workzeno.identity.auth.service.AuthService;
import com.workzeno.workzeno.identity.auth.service.RefreshCookieService;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;
import com.workzeno.workzeno.shared.exception.ApiException;

import com.workzeno.workzeno.shared.openapi.LoginOperation;
import com.workzeno.workzeno.shared.openapi.LogoutOperation;
import com.workzeno.workzeno.shared.openapi.RefreshTokenOperation;
import com.workzeno.workzeno.shared.openapi.RegisterOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.workzeno.workzeno.identity.auth.service.result.AuthenticationResult;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ApiResponseFactory responseFactory;
    private final RefreshCookieService refreshCookieService;
    private final AuthOriginValidator authOriginValidator;

    @RegisterOperation
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Registration request received");

        RegisterResponse result = authService.register(request);

        log.info("Registration completed successfully");

        ApiResponse<RegisterResponse> body = responseFactory.success(
                "Registration successful",
                result,
                httpRequest
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body);
    }

    @LoginOperation
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Login request received");
        AuthenticationResult result = authService.login(request);

        ResponseCookie refreshCookie =
                refreshCookieService.create(result.refreshToken());

        ApiResponse<AuthResponse> body = responseFactory.success(
                "Login successful",
                result.response(),
                httpRequest
        );

        log.info(
                "Login successful; issuing refresh cookie. path={}, secure={}, httpOnly=true",
                refreshCookie.getPath(),
                refreshCookie.isSecure()
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(body);
    }

    @RefreshTokenOperation
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(
                    name = RefreshCookieService.COOKIE_NAME,
                    required = false
            ) String refreshToken,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Refresh request received. origin={}, refreshCookiePresent={}",
                httpRequest.getHeader("Origin"),
                refreshToken != null && !refreshToken.isBlank()
        );

        authOriginValidator.validate(httpRequest);

        checkRefreshToken(refreshToken, httpRequest);

        AuthenticationResult result = authService.refresh(
                new RefreshRequest(refreshToken)
        );

        ResponseCookie refreshCookie =
                refreshCookieService.create(result.refreshToken());

        ApiResponse<AuthResponse> body = responseFactory.success(
                "Token refresh successful",
                result.response(),
                httpRequest
        );

        log.info("Refresh successful; rotated refresh cookie issued");

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(body);
    }



    @LogoutOperation
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(
                    name = RefreshCookieService.COOKIE_NAME,
                    required = false
            ) String refreshToken,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Logout request received. origin={}, refreshCookiePresent={}",
                httpRequest.getHeader("Origin"),
                refreshToken != null && !refreshToken.isBlank()
        );

        authOriginValidator.validate(httpRequest);

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(new RefreshRequest(refreshToken));
            log.info("Refresh session revoked during logout");
        } else {
            log.warn("Logout received without a refresh cookie");
        }

        ResponseCookie clearedCookie = refreshCookieService.clear();

        ApiResponse<Void> body = responseFactory.success(
                "Logout successful",
                null,
                httpRequest
        );

        log.info("Logout completed; refresh cookie clearing requested");

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, clearedCookie.toString())
                .body(body);
    }


    private static void checkRefreshToken(String refreshToken, HttpServletRequest httpRequest) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn(
                    "Refresh rejected: refresh cookie is missing. origin={}, uri={}",
                    httpRequest.getHeader("Origin"),
                    httpRequest.getRequestURI()
            );

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized",
                    "Refresh token is missing"
            );
        }
    }
}