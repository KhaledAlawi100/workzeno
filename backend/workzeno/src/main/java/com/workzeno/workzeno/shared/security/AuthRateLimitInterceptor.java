package com.workzeno.workzeno.shared.security;

import com.workzeno.workzeno.shared.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AuthRateLimitInterceptor implements HandlerInterceptor {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";

    private static final RateLimit LOGIN_LIMIT =
            new RateLimit(5, Duration.ofMinutes(1));

    private static final RateLimit REGISTER_LIMIT =
            new RateLimit(5, Duration.ofHours(1));

    private static final RateLimit REFRESH_LIMIT =
            new RateLimit(10, Duration.ofMinutes(1));

    private final ConcurrentHashMap<String, Window> windows =
            new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();

        RateLimit limit = getLimit(path);

        if (limit == null) {
            return true;
        }

        String clientIp = request.getRemoteAddr();

        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown";
        }

        String key = path + ":" + clientIp;

        long now = System.nanoTime();
        long windowNanos = limit.window().toNanos();

        AtomicReference<Decision> decision = new AtomicReference<>();

        windows.compute(key, (ignored, current) -> {
            if (current == null
                    || now - current.startedAtNanos() >= windowNanos) {

                decision.set(new Decision(true, 0));

                return new Window(now, 1, windowNanos);
            }

            if (current.requestCount() < limit.maxRequests()) {
                decision.set(new Decision(true, 0));

                return new Window(
                        current.startedAtNanos(),
                        current.requestCount() + 1,
                        windowNanos
                );
            }

            long remainingNanos =
                    windowNanos - (now - current.startedAtNanos());

            long retryAfterSeconds = Math.max(
                    1,
                    (remainingNanos + 999_999_999L) / 1_000_000_000L
            );

            decision.set(new Decision(false, retryAfterSeconds));

            return current;
        });

        cleanupExpiredWindows(now);

        Decision result = decision.get();

        if (!result.allowed()) {
            response.setHeader(
                    "Retry-After",
                    String.valueOf(result.retryAfterSeconds())
            );

            throw new ApiException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests",
                    "Too many requests. Please try again later."
            );
        }

        return true;
    }

    private RateLimit getLimit(String path) {
        return switch (path) {
            case LOGIN_PATH -> LOGIN_LIMIT;
            case REGISTER_PATH -> REGISTER_LIMIT;
            case REFRESH_PATH -> REFRESH_LIMIT;
            default -> null;
        };
    }

    private void cleanupExpiredWindows(long now) {
        if (windows.size() < 10_000) {
            return;
        }

        windows.entrySet().removeIf(entry -> {
            Window window = entry.getValue();

            return now - window.startedAtNanos()
                    >= window.windowNanos();
        });
    }

    private record RateLimit(
            int maxRequests,
            Duration window
    ) {}

    private record Window(
            long startedAtNanos,
            int requestCount,
            long windowNanos
    ) {}

    private record Decision(
            boolean allowed,
            long retryAfterSeconds
    ) {}
}