package org.fungover.zipp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final Map<String, LastRequestInfo> requestMap = new ConcurrentHashMap<>();
    private static final long TIME_WINDOW_MS = 60_000;
    private static final int MAX_REQUESTS = 5;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteAddr();
        long now = Instant.now().toEpochMilli();

        LastRequestInfo info = requestMap.getOrDefault(ip, new LastRequestInfo(0, now));

        if (now - info.windowStart > TIME_WINDOW_MS) {
            info.count = 0;
            info.windowStart = now;
        }

        info.count++;

        if (info.count > MAX_REQUESTS) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return false;
        }

        requestMap.put(ip, info);
        return true;
    }

    private static class LastRequestInfo {
        int count;
        long windowStart;

        LastRequestInfo(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
