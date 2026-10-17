
package com.workzeno.workzeno.identity.auth;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class ProtectedTestController {

    @GetMapping("/protected")
    public Map<String, Object> protectedEndpoint(
            Authentication authentication) {

        return Map.of(
                "authenticated", authentication.isAuthenticated(),
                "userId", authentication.getName()
        );
    }
}
