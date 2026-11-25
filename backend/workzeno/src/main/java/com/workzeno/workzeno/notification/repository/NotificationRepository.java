package com.workzeno.workzeno.notification.repository;

import com.workzeno.workzeno.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByRecipient_IdOrderByCreatedAtDescIdDesc(
            UUID recipientId,
            Pageable pageable
    );

    long countByRecipient_IdAndReadAtIsNull(UUID recipientId);

    Optional<Notification> findByIdAndRecipient_Id(
            UUID notificationId,
            UUID recipientId
    );

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.readAt = :readAt
        WHERE n.recipient.id = :recipientId
          AND n.readAt IS NULL
        """)
    int markAllAsRead(
            @Param("recipientId") UUID recipientId,
            @Param("readAt") Instant readAt
    );


}