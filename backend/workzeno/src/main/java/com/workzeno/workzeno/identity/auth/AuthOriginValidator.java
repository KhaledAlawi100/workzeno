
package com.workzeno.workzeno.identity.auth;

import com.workzeno.workzeno.shared.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthOriginValidator {

    private final String allowedOrigin;

    public AuthOriginValidator(
            @Value("${app.cors.allowed-origin}") String allowedOrigin
    ) {
        this.allowedOrigin = allowedOrigin;
    }

    public void validate(HttpServletRequest request) {
        String origin = request.getHeader("Origin");

        if (origin == null || !allowedOrigin.equals(origin)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    "Request origin is not allowed"
            );
        }
    }
}
