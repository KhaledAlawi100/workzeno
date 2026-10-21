
package com.workzeno.workzeno.identity.auth;

import com.workzeno.workzeno.identity.auth.dto.AuthResponse;
import com.workzeno.workzeno.identity.auth.dto.LoginResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthResponseMapper {

    public AuthResponse toAuthResponse(LoginResponse result) {
        return new AuthResponse(
                result.email(),
                result.role(),
                result.accessToken(),
                result.tokenType(),
                result.expiresIn()
        );
    }
}
