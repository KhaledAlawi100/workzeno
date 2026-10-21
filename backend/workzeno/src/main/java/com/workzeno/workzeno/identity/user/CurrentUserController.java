
package com.workzeno.workzeno.identity.user;

import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class CurrentUserController {

    private final UserRepository userRepository;
    private final ApiResponseFactory responseFactory;

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> getCurrentUser(
            Authentication authentication,
            HttpServletRequest request
    ) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "JWT authentication is required"
            );
        }

        UUID userId;
        try {
            userId = UUID.fromString(jwtAuth.getToken().getSubject());
        } catch (IllegalArgumentException exception) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Invalid user identity in access token"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user was not found"
                        )
                );

        return responseFactory.success(
                "Current user retrieved successfully",
                CurrentUserResponse.from(user),
                request
        );
    }
}
