package com.workzeno.workzeno.dashboard.dto;

public record ProjectMetricsResponse(
        long total,
        long active,
        long archived
) {
}