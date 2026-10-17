package com.workzeno.workzeno.shared.api;

import java.time.Instant;

import com.workzeno.workzeno.shared.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;


@Component
public class ApiResponseFactory {

    public <T> ApiResponse<T> success(
            String message,
            T data,
            HttpServletRequest request
    ) {
        Object requestId = request.getAttribute(
                RequestIdFilter.REQUEST_ID_ATTRIBUTE
        );

        return new ApiResponse<>(
                true,
                message,
                data,
                Instant.now(),
                request.getRequestURI(),
                requestId == null ? null : requestId.toString()
        );
    }
}

