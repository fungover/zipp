package org.fungover.zipp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final Map<String, LastRequestInfo> requestMap = new ConcurrentHashMap<>();
    private static final long TIME_WINDOW_MS = 60_000;
    private static final int MAX_REQUESTS = 5;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/reports")) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            response.setStatus(401);
            response.getWriter().write("Unauthorized");
            return false;
        }

        String userId = authentication.getName();
        long now = Instant.now().toEpochMilli();

        LastRequestInfo info = requestMap.compute(userId, (key, existing) -> {
            if (existing == null || now - existing.windowStart > TIME_WINDOW_MS) {
                return new LastRequestInfo(1, now);
            }
            return new LastRequestInfo(existing.count + 1, existing.windowStart);
        });

        if (info.count > MAX_REQUESTS) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return false;
        }

        return true;
    }

    private static class LastRequestInfo {
        final int count;
        final long windowStart;

        LastRequestInfo(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
