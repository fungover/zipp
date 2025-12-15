package org.fungover.zipp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RateLimitingInterceptorTest {

    private RateLimitingInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Authentication authentication;

    @BeforeEach
    void setup() throws Exception {
        interceptor = new RateLimitingInterceptor();

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // Mock URI and authentication
        when(request.getRequestURI()).thenReturn("/api/reports");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user1");

        // Mock response writer
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    }

    @Test
    void testAllowsFirstRequestsWithinLimit() throws Exception {
        // Make 5 requests (within limit)
        for (int i = 0; i < 5; i++) {
            boolean result = interceptor.preHandle(request, response, new Object());
            assertTrue(result, "Request " + i + " should be allowed");
        }
    }

    @Test
    void testBlocksWhenRateLimitExceeded() throws Exception {
        // Make 5 requests to reach the limit
        for (int i = 0; i < 5; i++) {
            interceptor.preHandle(request, response, new Object());
        }

        // 6th request should be blocked
        boolean result = interceptor.preHandle(request, response, new Object());
        assertEquals(false, result, "Request should be blocked after exceeding limit");
    }
}
