package org.fungover.zipp.entity;

import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.entity.ApiKey.KeyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyTest {

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @Test
        @DisplayName("should return true for active key without expiration")
        void shouldReturnTrueForActiveKeyWithoutExpiration() {
            ApiKey apiKey = createApiKey(KeyStatus.ACTIVE, null);

            assertThat(apiKey.isValid()).isTrue();
        }

        @Test
        @DisplayName("should return true for active key with future expiration")
        void shouldReturnTrueForActiveKeyWithFutureExpiration() {
            Instant futureDate = Instant.now().plus(30, ChronoUnit.DAYS);
            ApiKey apiKey = createApiKey(KeyStatus.ACTIVE, futureDate);

            assertThat(apiKey.isValid()).isTrue();
        }

        @Test
        @DisplayName("should return false for revoked key")
        void shouldReturnFalseForRevokedKey() {
            ApiKey apiKey = createApiKey(KeyStatus.REVOKED, null);

            assertThat(apiKey.isValid()).isFalse();
        }

        @Test
        @DisplayName("should return false for expired key status")
        void shouldReturnFalseForExpiredKeyStatus() {
            ApiKey apiKey = createApiKey(KeyStatus.EXPIRED, null);

            assertThat(apiKey.isValid()).isFalse();
        }

        @Test
        @DisplayName("should return false for key past expiration date")
        void shouldReturnFalseForKeyPastExpirationDate() {
            Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
            ApiKey apiKey = createApiKey(KeyStatus.ACTIVE, pastDate);

            assertThat(apiKey.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasScope")
    class HasScope {

        @Test
        @DisplayName("should return true when scope exists")
        void shouldReturnTrueWhenScopeExists() {
            ApiKey apiKey = createApiKey(KeyStatus.ACTIVE, null);
            apiKey.setScopes(Set.of(ApiScope.REPORTS_READ, ApiScope.STATS_READ));

            assertThat(apiKey.hasScope(ApiScope.REPORTS_READ)).isTrue();
            assertThat(apiKey.hasScope(ApiScope.STATS_READ)).isTrue();
        }

        @Test
        @DisplayName("should return false when scope does not exist")
        void shouldReturnFalseWhenScopeDoesNotExist() {
            ApiKey apiKey = createApiKey(KeyStatus.ACTIVE, null);
            apiKey.setScopes(Set.of(ApiScope.REPORTS_READ));

            assertThat(apiKey.hasScope(ApiScope.STATS_READ)).isFalse();
            assertThat(apiKey.hasScope(ApiScope.REPORTS_WRITE)).isFalse();
        }

        @Test
        @DisplayName("should return false when scopes are empty")
        void shouldReturnFalseWhenScopesEmpty() {
            ApiKey apiKey = createApiKey(KeyStatus.ACTIVE, null);
            apiKey.setScopes(Set.of());

            assertThat(apiKey.hasScope(ApiScope.REPORTS_READ)).isFalse();
        }
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should create key with required fields")
        void shouldCreateKeyWithRequiredFields() {
            UUID userId = UUID.randomUUID();
            String keyHash = "testhash123";
            String keyPrefix = "zipp_live_te";
            String name = "Test Key";

            ApiKey apiKey = new ApiKey(userId, keyHash, keyPrefix, name);

            assertThat(apiKey.getUserId()).isEqualTo(userId);
            assertThat(apiKey.getKeyHash()).isEqualTo(keyHash);
            assertThat(apiKey.getKeyPrefix()).isEqualTo(keyPrefix);
            assertThat(apiKey.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("should have default ACTIVE status")
        void shouldHaveDefaultActiveStatus() {
            ApiKey apiKey = new ApiKey();

            assertThat(apiKey.getStatus()).isEqualTo(KeyStatus.ACTIVE);
        }
    }

    private ApiKey createApiKey(KeyStatus status, Instant expiresAt) {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.randomUUID());
        apiKey.setUserId(UUID.randomUUID());
        apiKey.setKeyHash("testhash");
        apiKey.setKeyPrefix("zipp_live_te");
        apiKey.setName("Test Key");
        apiKey.setStatus(status);
        apiKey.setExpiresAt(expiresAt);
        apiKey.setCreatedAt(Instant.now());
        return apiKey;
    }
}
