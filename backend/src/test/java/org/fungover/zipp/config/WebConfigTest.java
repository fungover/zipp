package org.fungover.zipp.config;

import org.fungover.zipp.interceptor.RateLimitingInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class WebConfigTest {

    @Test
    void testInterceptorRegistration() {
        RateLimitingInterceptor interceptor = mock(RateLimitingInterceptor.class);
        WebConfig config = new WebConfig(interceptor);

        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class);

        // Make registry.addInterceptor return a real mock InterceptorRegistration
        when(registry.addInterceptor(interceptor)).thenReturn(registration);

        config.addInterceptors(registry);

        // Verify interceptor was registered
        verify(registry).addInterceptor(interceptor);

        // Verify the path pattern was added
        verify(registration).addPathPatterns("/api/reports");
    }
}
