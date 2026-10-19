
package com.workzeno.workzeno.identity.auth;

import com.workzeno.workzeno.identity.auth.dto.*;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ApiResponseFactory responseFactory;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        RegisterResponse result = authService.register(request);

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
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResponse result = authService.login(request);

        ApiResponse<LoginResponse> body = responseFactory.success(
                "Login successful",
                result,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }


    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResponse result = authService.refresh(request);

        ApiResponse<LoginResponse> body = responseFactory.success(
                "Token refresh successful",
                result,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.logout(request);

        ApiResponse<Void> body = responseFactory.success(
                "Logout successful",
                null,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }

}
