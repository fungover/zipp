package org.fungover.zipp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.exception.InvalidApiKeyException;
import org.fungover.zipp.service.ApiKeyService;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Filters that intercept incoming requests and validate API keys.
 * Looking for the key in the X-API-Key header.
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        // Only process if API key exists and path requires it
        if (apiKey != null && shouldAuthenticateWithApiKey(request)) {
            try {
                // Validate key - throws InvalidApiKeyException if invalid
                ApiKey validatedKey = apiKeyService.validateApiKey(apiKey);

                // Create authentication tokens with scope-based authorities
                ApiKeyAuthentication authentication = new ApiKeyAuthentication(
                    validatedKey.getUserId(),
                    validatedKey.getId(),
                    validatedKey.getScopes()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (InvalidApiKeyException e) {
                // Invalid API key - return 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\": \"Unauthorized\", \"message\": \"" + e.getMessage() + "\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determines which paths should be authenticated with API key.
     */
    private boolean shouldAuthenticateWithApiKey(HttpServletRequest request) {
        String path = request.getRequestURI();
        // GraphQL and M2M API endpoints require API key
        return path.startsWith("/graphql") || path.startsWith("/api/m2m/");
    }

    /**
     * Custom authentication token for API key-authenticated requests.
     */
    public static class ApiKeyAuthentication extends AbstractAuthenticationToken {

        private final UUID userId;
        private final UUID apiKeyId;
        private final Set<ApiScope> scopes;

        public ApiKeyAuthentication(UUID userId, UUID apiKeyId, Set<ApiScope> scopes) {
            super(buildAuthorities(scopes));
            this.userId = userId;
            this.apiKeyId = apiKeyId;
            this.scopes = scopes;
            setAuthenticated(true);
        }

        /**
         * Building Spring Security authorities from API scopes.
         */
        private static List<SimpleGrantedAuthority> buildAuthorities(Set<ApiScope> scopes) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            // All API keys get ROLE_API_CLIENT
            authorities.add(new SimpleGrantedAuthority("ROLE_API_CLIENT"));

            // Add scope-based authorities
            if (scopes != null) {
                for (ApiScope scope : scopes) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope.name()));
                }
            }
            return authorities;
        }

        @Override
        public Object getCredentials() {
            return null;  // API key is not saved after authentication
        }

        @Override
        public Object getPrincipal() {
            return userId;
        }

        public UUID getUserId() {
            return userId;
        }

        public UUID getApiKeyId() {
            return apiKeyId;
        }

        public Set<ApiScope> getScopes() {
            return scopes;
        }

        public boolean hasScope(ApiScope scope) {
            return scopes != null && scopes.contains(scope);
        }
    }
}
