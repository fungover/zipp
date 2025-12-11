package org.fungover.zipp.controller;

import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.security.ApiKeyAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * M2M API – endpoints för maskin-till-maskin-anrop som autentiseras via X-API-Key.
 *
 * Alla endpoints här ligger bakom:
 *  - ApiKeyAuthenticationFilter (X-API-Key)
 *  - SecurityConfig (ROLE_API_CLIENT)
 */
@RestController
@RequestMapping("/api/m2m")
class M2MController {

    /**
     * Enkel ping-endpoint för att testa att M2M-auth fungerar.
     *
     * GET /api/m2m/ping
     * Header: X-API-Key: <din nyckel>
     */
    @GetMapping("/ping")
    public Map<String, Object> ping(Authentication authentication) {
        ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth = getApiKeyAuth(authentication);

        return Map.of(
            "status", "ok",
            "message", "M2M API is reachable",
            "userId", apiAuth.getUserId(),
            "apiKeyId", apiAuth.getApiKeyId(),
            "scopes", apiAuth.getScopes(),
            "timestamp", Instant.now().toString()
        );
    }

    /**
     * Exempel-endpoint som kräver REPORTS_READ-scope.
     *
     * GET /api/m2m/reports
     */
    @GetMapping("/reports")
    public Map<String, Object> getReports(Authentication authentication) {
        ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth = getApiKeyAuth(authentication);

        requireScope(apiAuth, ApiScope.REPORTS_READ);

        // TODO: här skulle du i framtiden hämta riktiga reports från ett service-lager
        return Map.of(
            "status", "ok",
            "message", "REPORTS_READ scope granted",
            "userId", apiAuth.getUserId(),
            "scopes", apiAuth.getScopes()
        );
    }

    /**
     * Exempel-endpoint som kräver STATS_READ-scope.
     *
     * GET /api/m2m/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats(Authentication authentication) {
        ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth = getApiKeyAuth(authentication);

        requireScope(apiAuth, ApiScope.STATS_READ);

        // TODO: här skulle du i framtiden hämta statistikdata från ett service-lager
        return Map.of(
            "status", "ok",
            "message", "STATS_READ scope granted",
            "userId", apiAuth.getUserId(),
            "scopes", apiAuth.getScopes()
        );
    }

    // ------------------------
    //   HJÄLP-METODER
    // ------------------------

    /**
     * Säkerställer att Authentication är av typen ApiKeyAuthentication.
     * Kastar IllegalStateException om någon annan auth-typ hamnat här.
     */
    private ApiKeyAuthenticationFilter.ApiKeyAuthentication getApiKeyAuth(Authentication authentication) {
        if (authentication instanceof ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth) {
            return apiAuth;
        }
        throw new IllegalStateException(
            "Unexpected authentication type: " +
                (authentication != null ? authentication.getClass() : "null")
        );
    }

    /**
     * Kastar AccessDeniedException om API-nyckeln saknar ett visst scope.
     * Spring Security översätter den till 403 Forbidden.
     */
    private void requireScope(ApiKeyAuthenticationFilter.ApiKeyAuthentication apiAuth, ApiScope requiredScope) {
        Set<ApiScope> scopes = apiAuth.getScopes();
        if (!scopes.contains(requiredScope)) {
            throw new AccessDeniedException("Missing required scope: " + requiredScope.name());
        }
    }
}
