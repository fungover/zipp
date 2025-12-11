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
 * Filter som fångar upp inkommande requests och validerar API-nycklar.
 * Letar efter nyckeln i X-API-Key headern.
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

        // Endast processa om API-nyckel finns och path kräver det
        if (apiKey != null && shouldAuthenticateWithApiKey(request)) {
            try {
                // Validera nyckeln - kastar InvalidApiKeyException om ogiltig
                ApiKey validatedKey = apiKeyService.validateApiKey(apiKey);

                // Skapa authentication token med scope-baserade authorities
                ApiKeyAuthentication authentication = new ApiKeyAuthentication(
                    validatedKey.getUserId(),
                    validatedKey.getId(),
                    validatedKey.getScopes()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (InvalidApiKeyException e) {
                // Ogiltig API-nyckel - returnera 401
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
     * Bestämmer vilka paths som ska autentiseras med API-nyckel.
     */
    private boolean shouldAuthenticateWithApiKey(HttpServletRequest request) {
        String path = request.getRequestURI();
        // GraphQL och M2M API endpoints kräver API-nyckel
        return path.startsWith("/graphql") || path.startsWith("/api/m2m/");
    }

    /**
     * Custom authentication token för API-nyckel-autentiserade requests.
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
         * Bygger Spring Security authorities från API-scopes.
         */
        private static List<SimpleGrantedAuthority> buildAuthorities(Set<ApiScope> scopes) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            // Alla API-nycklar får ROLE_API_CLIENT
            authorities.add(new SimpleGrantedAuthority("ROLE_API_CLIENT"));

            // Lägg till scope-baserade authorities
            if (scopes != null) {
                for (ApiScope scope : scopes) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope.name()));
                }
            }
            return authorities;
        }

        @Override
        public Object getCredentials() {
            return null;  // API-nyckeln sparas inte efter autentisering
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
