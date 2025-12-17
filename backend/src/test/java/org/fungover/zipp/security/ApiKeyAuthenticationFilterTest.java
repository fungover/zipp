package org.fungover.zipp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.entity.ApiKey.KeyStatus;
import org.fungover.zipp.exception.InvalidApiKeyException;
import org.fungover.zipp.service.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiKeyAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("doFilterInternal")
    class DoFilterInternal {

        @Test
        @DisplayName("should authenticate with valid API key on m2m path")
        void shouldAuthenticateWithValidApiKeyOnM2mPath() throws Exception {
            ApiKey validKey = createValidApiKey();
            when(request.getHeader("X-API-Key")).thenReturn("zipp_live_validkey");
            when(request.getServletPath()).thenReturn("/api/m2m/ping");
            when(apiKeyService.validateApiKey("zipp_live_validkey")).thenReturn(validKey);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        }

        @Test
        @DisplayName("should authenticate with valid API key on graphql path")
        void shouldAuthenticateWithValidApiKeyOnGraphqlPath() throws Exception {
            ApiKey validKey = createValidApiKey();
            when(request.getHeader("X-API-Key")).thenReturn("zipp_live_validkey");
            when(request.getServletPath()).thenReturn("/graphql");
            when(apiKeyService.validateApiKey("zipp_live_validkey")).thenReturn(validKey);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        }

        @Test
        @DisplayName("should return 401 for invalid API key")
        void shouldReturn401ForInvalidApiKey() throws Exception {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            when(request.getHeader("X-API-Key")).thenReturn("invalid_key");
            when(request.getServletPath()).thenReturn("/api/m2m/ping");
            when(apiKeyService.validateApiKey("invalid_key"))
                .thenThrow(new InvalidApiKeyException("Invalid API key"));
            when(response.getWriter()).thenReturn(printWriter);

            filter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("should skip authentication for non m2m paths")
        void shouldSkipAuthenticationForNonM2mPaths() throws Exception {
            when(request.getHeader("X-API-Key")).thenReturn("zipp_live_validkey");
            when(request.getServletPath()).thenReturn("/api/other");

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(apiKeyService, never()).validateApiKey(anyString());
        }

        @Test
        @DisplayName("should skip authentication when no API key provided")
        void shouldSkipAuthenticationWhenNoApiKey() throws Exception {
            when(request.getHeader("X-API-Key")).thenReturn(null);
            // Ta bort: when(request.getServletPath()).thenReturn("/api/m2m/ping");

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(apiKeyService, never()).validateApiKey(anyString());
        }
    }

    @Nested
    @DisplayName("ApiKeyAuthentication")
    class ApiKeyAuthenticationTest {

        @Test
        @DisplayName("should build correct authorities from scopes")
        void shouldBuildCorrectAuthoritiesFromScopes() {
            Set<ApiScope> scopes = Set.of(ApiScope.REPORTS_READ, ApiScope.STATS_READ);

            ApiKeyAuthenticationFilter.ApiKeyAuthentication auth =
                new ApiKeyAuthenticationFilter.ApiKeyAuthentication(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    scopes
                );

            assertThat(auth.getAuthorities())
                .extracting("authority")
                .contains("ROLE_API_CLIENT", "SCOPE_REPORTS_READ", "SCOPE_STATS_READ");
        }

        @Test
        @DisplayName("should have ROLE_API_CLIENT even with empty scopes")
        void shouldHaveRoleApiClientWithEmptyScopes() {
            ApiKeyAuthenticationFilter.ApiKeyAuthentication auth =
                new ApiKeyAuthenticationFilter.ApiKeyAuthentication(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Set.of()
                );

            assertThat(auth.getAuthorities())
                .extracting("authority")
                .contains("ROLE_API_CLIENT");
        }

        @Test
        @DisplayName("should check scope correctly")
        void shouldCheckScopeCorrectly() {
            ApiKeyAuthenticationFilter.ApiKeyAuthentication auth =
                new ApiKeyAuthenticationFilter.ApiKeyAuthentication(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Set.of(ApiScope.REPORTS_READ)
                );

            assertThat(auth.hasScope(ApiScope.REPORTS_READ)).isTrue();
            assertThat(auth.hasScope(ApiScope.STATS_READ)).isFalse();
        }
    }

    private ApiKey createValidApiKey() {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.randomUUID());
        apiKey.setUserId(UUID.randomUUID());
        apiKey.setKeyHash("testhash");
        apiKey.setKeyPrefix("zipp_live_te");
        apiKey.setName("Test Key");
        apiKey.setScopes(Set.of(ApiScope.REPORTS_READ, ApiScope.STATS_READ));
        apiKey.setStatus(KeyStatus.ACTIVE);
        apiKey.setCreatedAt(Instant.now());
        return apiKey;
    }
}
