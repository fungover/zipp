package org.fungover.zipp.controller;

import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.security.ApiKeyAuthenticationFilter.ApiKeyAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class M2MControllerTest {

    private final M2MController controller = new M2MController();

    @Nested
    @DisplayName("ping endpoint")
    class PingEndpoint {

        @Test
        @DisplayName("should return ok status with valid authentication")
        void shouldReturnOkStatusWithValidAuthentication() {
            UUID userId = UUID.randomUUID();
            UUID apiKeyId = UUID.randomUUID();
            Set<ApiScope> scopes = Set.of(ApiScope.REPORTS_READ);

            ApiKeyAuthentication auth = new ApiKeyAuthentication(userId, apiKeyId, scopes);

            Map<String, Object> result = controller.ping(auth);

            assertThat(result.get("status")).isEqualTo("ok");
            assertThat(result.get("message")).isEqualTo("M2M API is reachable");
            assertThat(result.get("userId")).isEqualTo(userId);
            assertThat(result.get("apiKeyId")).isEqualTo(apiKeyId);
        }

        @Test
        @DisplayName("should throw exception for wrong authentication type")
        void shouldThrowExceptionForWrongAuthenticationType() {
            UsernamePasswordAuthenticationToken wrongAuth = new UsernamePasswordAuthenticationToken("user", "pass");

            assertThatThrownBy(() -> controller.ping(wrongAuth))
                    .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                    .hasMessageContaining("Missing API key authentication");
        }
    }

    @Nested
    @DisplayName("reports endpoint")
    class ReportsEndpoint {

        @Test
        @DisplayName("should return ok when REPORTS_READ scope present")
        void shouldReturnOkWhenReportsReadScopePresent() {
            UUID userId = UUID.randomUUID();
            UUID apiKeyId = UUID.randomUUID();
            Set<ApiScope> scopes = Set.of(ApiScope.REPORTS_READ);

            ApiKeyAuthentication auth = new ApiKeyAuthentication(userId, apiKeyId, scopes);

            Map<String, Object> result = controller.getReports(auth);

            assertThat(result.get("status")).isEqualTo("ok");
            assertThat(result.get("message")).isEqualTo("REPORTS_READ scope granted");
        }

        @Test
        @DisplayName("should throw AccessDeniedException when REPORTS_READ scope missing")
        void shouldThrowAccessDeniedWhenReportsReadScopeMissing() {
            UUID userId = UUID.randomUUID();
            UUID apiKeyId = UUID.randomUUID();
            Set<ApiScope> scopes = Set.of(ApiScope.STATS_READ);

            ApiKeyAuthentication auth = new ApiKeyAuthentication(userId, apiKeyId, scopes);

            assertThatThrownBy(() -> controller.getReports(auth)).isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Missing required scope: REPORTS_READ");
        }
    }

    @Nested
    @DisplayName("stats endpoint")
    class StatsEndpoint {

        @Test
        @DisplayName("should return ok when STATS_READ scope present")
        void shouldReturnOkWhenStatsReadScopePresent() {
            UUID userId = UUID.randomUUID();
            UUID apiKeyId = UUID.randomUUID();
            Set<ApiScope> scopes = Set.of(ApiScope.STATS_READ);

            ApiKeyAuthentication auth = new ApiKeyAuthentication(userId, apiKeyId, scopes);

            Map<String, Object> result = controller.getStats(auth);

            assertThat(result.get("status")).isEqualTo("ok");
            assertThat(result.get("message")).isEqualTo("STATS_READ scope granted");
        }

        @Test
        @DisplayName("should throw AccessDeniedException when STATS_READ scope missing")
        void shouldThrowAccessDeniedWhenStatsReadScopeMissing() {
            UUID userId = UUID.randomUUID();
            UUID apiKeyId = UUID.randomUUID();
            Set<ApiScope> scopes = Set.of(ApiScope.REPORTS_READ);

            ApiKeyAuthentication auth = new ApiKeyAuthentication(userId, apiKeyId, scopes);

            assertThatThrownBy(() -> controller.getStats(auth)).isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Missing required scope: STATS_READ");
        }
    }
}
