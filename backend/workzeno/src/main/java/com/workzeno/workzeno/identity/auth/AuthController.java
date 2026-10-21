package com.workzeno.workzeno.identity.auth;

import com.workzeno.workzeno.identity.auth.dto.*;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;
import com.workzeno.workzeno.shared.exception.ApiException;

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

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ApiResponseFactory responseFactory;
    private final RefreshCookieService refreshCookieService;
    private final AuthResponseMapper authResponseMapper;
    private final AuthOriginValidator authOriginValidator;

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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Login request received");

        LoginResponse result = authService.login(request);

        ResponseCookie refreshCookie =
                refreshCookieService.create(result.refreshToken());

        AuthResponse authResponse =
                authResponseMapper.toAuthResponse(result);

        ApiResponse<AuthResponse> body = responseFactory.success(
                "Login successful",
                authResponse,
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

        LoginResponse result = authService.refresh(
                new RefreshRequest(refreshToken)
        );

        ResponseCookie refreshCookie =
                refreshCookieService.create(result.refreshToken());

        AuthResponse authResponse =
                authResponseMapper.toAuthResponse(result);

        ApiResponse<AuthResponse> body = responseFactory.success(
                "Token refresh successful",
                authResponse,
                httpRequest
        );

        log.info("Refresh successful; rotated refresh cookie issued");

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(body);
    }

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
}