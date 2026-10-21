
package com.workzeno.workzeno.identity.user;

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
