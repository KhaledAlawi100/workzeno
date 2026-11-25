package com.workzeno.workzeno.notification.domain;

import com.workzeno.workzeno.identity.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
public class Notification {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "recipient_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notifications_recipient")
    )
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "actor_id",
            foreignKey = @ForeignKey(name = "fk_notifications_actor")
    )
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @Column(name = "reference_type", length = 40)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
        // Required by JPA.
    }

    public Notification(
            User recipient,
            User actor,
            NotificationType type,
            String title,
            String message,
            String targetUrl,
            String referenceType,
            UUID referenceId
    ) {
        if (recipient == null) {
            throw new IllegalArgumentException("Notification recipient is required");
        }

        if (type == null) {
            throw new IllegalArgumentException("Notification type is required");
        }

        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.title = requireText(title, 120, "Notification title");
        this.message = requireText(message, 500, "Notification message");
        this.targetUrl = normalizeOptional(targetUrl, 500, "Target URL");
        this.referenceType = normalizeOptional(
                referenceType,
                40,
                "Reference type"
        );
        this.referenceId = referenceId;

        if ((this.referenceType == null) != (this.referenceId == null)) {
            throw new IllegalArgumentException(
                    "Reference type and reference ID must both be provided or both be null"
            );
        }
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead(Instant readAt) {
        if (readAt == null) {
            throw new IllegalArgumentException("Read time is required");
        }

        if (this.readAt == null) {
            this.readAt = readAt;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private static String requireText(
            String value,
            int maxLength,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed " + maxLength + " characters"
            );
        }

        return normalized;
    }

    private static String normalizeOptional(
            String value,
            int maxLength,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed " + maxLength + " characters"
            );
        }

        return normalized;
    }
}