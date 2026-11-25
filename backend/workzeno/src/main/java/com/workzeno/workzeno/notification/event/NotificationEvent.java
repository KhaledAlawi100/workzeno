package com.workzeno.workzeno.notification.event;

import com.workzeno.workzeno.notification.domain.NotificationType;

import java.util.UUID;

public record NotificationEvent(
        UUID recipientId,
        UUID actorId,
        NotificationType type,
        String title,
        String message,
        String targetUrl,
        String referenceType,
        UUID referenceId
) {
}