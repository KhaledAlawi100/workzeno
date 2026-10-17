package com.workzeno.workzeno.shared.api;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp,
        String path,
        String requestId
) {
}

