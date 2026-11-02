
package com.workzeno.workzeno.identity.user.dto;

import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.domain.UserRole;
import com.workzeno.workzeno.identity.user.domain.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        UserRole role,
        UserStatus status,
        Instant createdAt
) {
    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
