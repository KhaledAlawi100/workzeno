
package com.workzeno.workzeno.identity.auth;

import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshSessionRevocationService {

    private final RefreshSessionRepository refreshSessionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int revokeFamily(UUID familyId, Instant revokedAt) {
        return refreshSessionRepository.revokeActiveSessionsInFamily(
                familyId,
                revokedAt
        );
    }
}
