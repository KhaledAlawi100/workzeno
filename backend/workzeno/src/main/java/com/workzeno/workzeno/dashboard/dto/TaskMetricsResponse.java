package com.workzeno.workzeno.dashboard.dto;

public record TaskMetricsResponse(
        long total,
        long todo,
        long inProgress,
        long inReview,
        long done,
        long overdue,
        long assignedToMe
) {
}