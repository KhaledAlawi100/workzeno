package com.workzeno.workzeno.notification.controller;

import com.workzeno.workzeno.notification.dto.NotificationResponse;
import com.workzeno.workzeno.notification.dto.UnreadNotificationCountResponse;
import com.workzeno.workzeno.notification.service.NotificationService;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;
    private final ApiResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>>
    getMyNotifications(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must not be negative")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            int size,

            HttpServletRequest request
    ) {
        Page<NotificationResponse> result =
                notificationService.getMyNotifications(
                        PageRequest.of(page, size)
                );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Notifications retrieved successfully",
                        result,
                        request
                )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadNotificationCountResponse>>
    getUnreadCount(HttpServletRequest request) {

        UnreadNotificationCountResponse result =
                notificationService.getUnreadCount();

        return ResponseEntity.ok(
                responseFactory.success(
                        "Unread notification count retrieved successfully",
                        result,
                        request
                )
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>>
    markAsRead(
            @PathVariable UUID notificationId,
            HttpServletRequest request
    ) {
        NotificationResponse result =
                notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(
                responseFactory.success(
                        "Notification marked as read",
                        result,
                        request
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>>
    markAllAsRead(HttpServletRequest request) {

        int updatedCount = notificationService.markAllAsRead();

        return ResponseEntity.ok(
                responseFactory.success(
                        "All notifications marked as read",
                        Map.of("updatedCount", updatedCount),
                        request
                )
        );
    }
}