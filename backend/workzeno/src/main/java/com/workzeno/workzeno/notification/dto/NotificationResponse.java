package com.workzeno.workzeno.notification.dto;

import com.workzeno.workzeno.notification.domain.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        String targetUrl,
        String referenceType,
        UUID referenceId,
        String actorEmail,
        Instant readAt,
        Instant createdAt,
        boolean read
) {
}