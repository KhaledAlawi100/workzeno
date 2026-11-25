package com.workzeno.workzeno.notification.service;

import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.notification.domain.Notification;
import com.workzeno.workzeno.notification.dto.NotificationResponse;
import com.workzeno.workzeno.notification.dto.UnreadNotificationCountResponse;
import com.workzeno.workzeno.notification.mapper.NotificationMapper;
import com.workzeno.workzeno.notification.repository.NotificationRepository;
import com.workzeno.workzeno.shared.exception.ApiException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.notification.domain.Notification;
import com.workzeno.workzeno.notification.event.NotificationEvent;

import org.springframework.transaction.annotation.Propagation;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    public Page<NotificationResponse> getMyNotifications(
            Pageable pageable
    ) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        return notificationRepository
                .findAllByRecipient_IdOrderByCreatedAtDescIdDesc(
                        currentUserId,
                        pageable
                )
                .map(notificationMapper::toResponse);
    }

    public UnreadNotificationCountResponse getUnreadCount() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        long count = notificationRepository
                .countByRecipient_IdAndReadAtIsNull(currentUserId);

        return new UnreadNotificationCountResponse(count);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Notification notification = notificationRepository
                .findByIdAndRecipient_Id(notificationId, currentUserId)
                .orElseThrow(this::notificationNotFound);

        notification.markAsRead(Instant.now());

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public int markAllAsRead() {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        return notificationRepository.markAllAsRead(
                currentUserId,
                Instant.now()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFromEvent(NotificationEvent event) {

        if (event.recipientId().equals(event.actorId())) {
            return;
        }

        User recipient = userRepository.findById(event.recipientId())
                .orElse(null);

        if (recipient == null) {
            return;
        }

        User actor = event.actorId() == null
                ? null
                : userRepository.findById(event.actorId()).orElse(null);

        Notification notification = new Notification(
                recipient,
                actor,
                event.type(),
                event.title(),
                event.message(),
                event.targetUrl(),
                event.referenceType(),
                event.referenceId()
        );

        notificationRepository.save(notification);
    }

    private ApiException notificationNotFound() {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                "Notification not found",
                "The notification does not exist or does not belong to the current user"
        );
    }
}