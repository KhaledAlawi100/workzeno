package com.workzeno.workzeno.identity.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


public interface RefreshSessionRepository
        extends JpaRepository<RefreshSession, UUID> {

    // ============================================================
    // Rotation lookup — locked read
    // ============================================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select session
        from RefreshSession session
        where session.tokenHash = :tokenHash
        """)
    Optional<RefreshSession> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    // ============================================================
    // Family-wide revocation — reuse detection / logout-everywhere
    // ============================================================

@Modifying
    @Query("""
    update RefreshSession session
       set session.revokedAt = :revokedAt
     where session.familyId = :familyId
       and session.revokedAt is null
""")
    int revokeActiveSessionsInFamily(
            @Param("familyId") UUID familyId,
            @Param("revokedAt") Instant revokedAt
    );
}