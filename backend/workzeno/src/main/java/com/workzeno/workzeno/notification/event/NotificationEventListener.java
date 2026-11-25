package com.workzeno.workzeno.notification.event;

import com.workzeno.workzeno.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationEvent event) {

        try {
            notificationService.createFromEvent(event);

        } catch (RuntimeException exception) {
            log.error(
                    "Failed to create notification. recipientId={}, type={}",
                    event.recipientId(),
                    event.type(),
                    exception
            );
        }
    }
}