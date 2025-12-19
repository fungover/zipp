package org.fungover.zipp.controller;

import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.security.ApiKeyAuthenticationFilter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * M2M API – endpoints for machine-to-machine-calls that are authenticated via
 * X-API-Key. All endpoints are behind: - ApiKeyAuthenticationFilter (X-API-Key)
 * - SecurityConfig (ROLE_API_CLIENT)
 */

@RestController
@RequestMapping("/api/m2m")
class M2MController {

    /**
     * Simple ping-endpoint to verify that M2M-auth works. GET /api/m2m/ping Header:
     * X-API-Key: <your key>
     */

    @GetMapping("/ping")
    public Map<String, Object> ping(Authentication authentication) {
        ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth = getApiKeyAuth(authentication);

        return Map.of("status", "ok", "message", "M2M API is reachable", "userId", apiAuth.getUserId(), "apiKeyId",
                apiAuth.getApiKeyId(), "scopes", apiAuth.getScopes(), "timestamp", Instant.now().toString());
    }

    /**
     * Exampel-endpoint that requires REPORTS_READ-scope. GET /api/m2m/reports
     */

    @GetMapping("/reports")
    public Map<String, Object> getReports(Authentication authentication) {
        ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth = getApiKeyAuth(authentication);

        requireScope(apiAuth, ApiScope.REPORTS_READ);

        // Future improvements?
        return Map.of("status", "ok", "message", "REPORTS_READ scope granted", "userId", apiAuth.getUserId(), "scopes",
                apiAuth.getScopes());
    }

    /**
     * Exampel-endpoint that requires STATS_READ-scope. GET /api/m2m/stats
     */

    @GetMapping("/stats")
    public Map<String, Object> getStats(Authentication authentication) {
        ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth = getApiKeyAuth(authentication);

        requireScope(apiAuth, ApiScope.STATS_READ);

        // Future improvements?
        return Map.of("status", "ok", "message", "STATS_READ scope granted", "userId", apiAuth.getUserId(), "scopes",
                apiAuth.getScopes());
    }

    // ------------------------
    // HELPER METHODS
    // ------------------------

    /**
     * Authentication has to be of the type -ApiKeyAuthentication. Throws
     * IllegalStateException if another auth-type slips in.
     */

    private ApiKeyAuthenticationFilter.ApiKeyAuthentication getApiKeyAuth(Authentication authentication) {
        if (authentication instanceof ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth) {
            return apiAuth;
        }
        throw new AuthenticationCredentialsNotFoundException("Missing API key authentication");
    }

    /**
     * Throws AccessDeniedException if API-key misses a specific scope. Spring
     * Security translates it to 403 Forbidden.
     */
    private void requireScope(ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth, ApiScope requiredScope) {
        Set<ApiScope> scopes = apiAuth.getScopes();
        if (scopes == null || !scopes.contains(requiredScope)) {
            throw new AccessDeniedException("Missing required scope: " + requiredScope.name());
        }
    }
}
