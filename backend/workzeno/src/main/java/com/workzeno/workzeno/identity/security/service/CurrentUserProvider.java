package com.workzeno.workzeno.identity.security.service;

import com.workzeno.workzeno.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {

    public UUID getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || !authentication.isAuthenticated()) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized",
                    "A valid authenticated user is required"
            );
        }

        String subject = jwtAuthentication.getToken().getSubject();

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized",
                    "The authentication subject is invalid"
            );
        }
    }
}