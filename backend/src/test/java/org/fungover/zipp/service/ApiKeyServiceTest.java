package org.fungover.zipp.service;

import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.entity.ApiKey.KeyStatus;
import org.fungover.zipp.exception.InvalidApiKeyException;
import org.fungover.zipp.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @InjectMocks
    private ApiKeyService apiKeyService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("createApiKey")
    class CreateApiKey {

        @Test
        @DisplayName("should create API key with valid parameters")
        void shouldCreateApiKeyWithValidParameters() {
            when(apiKeyRepository.countActiveKeysByUserId(userId)).thenReturn(0L);
            when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
                ApiKey key = invocation.getArgument(0);
                key.setId(UUID.randomUUID());
                return key;
            });

            ApiKeyService.CreatedApiKey result = apiKeyService.createApiKey(userId, "Test Key", "Test Description",
                    Set.of(ApiScope.REPORTS_READ), null);

            assertThat(result.plainKey()).startsWith("zipp_live_");
            assertThat(result.apiKey().getName()).isEqualTo("Test Key");
            assertThat(result.apiKey().getDescription()).isEqualTo("Test Description");
            assertThat(result.apiKey().getScopes()).contains(ApiScope.REPORTS_READ);
            assertThat(result.apiKey().getStatus()).isEqualTo(KeyStatus.ACTIVE);
        }

        @Test
        @DisplayName("should throw exception when max keys reached")
        void shouldThrowExceptionWhenMaxKeysReached() {
            when(apiKeyRepository.countActiveKeysByUserId(userId)).thenReturn(10L);

            assertThatThrownBy(() -> apiKeyService.createApiKey(userId, "Too Many Keys", null,
                    Set.of(ApiScope.REPORTS_READ), null)).isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Maximum number of active API keys reached");
        }

        @Test
        @DisplayName("should generate key with correct prefix")
        void shouldGenerateKeyWithCorrectPrefix() {
            when(apiKeyRepository.countActiveKeysByUserId(userId)).thenReturn(0L);
            when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> {
                ApiKey key = invocation.getArgument(0);
                key.setId(UUID.randomUUID());
                return key;
            });

            ApiKeyService.CreatedApiKey result = apiKeyService.createApiKey(userId, "Prefix Test", null, Set.of(),
                    null);

            assertThat(result.apiKey().getKeyPrefix()).hasSize(12);
            assertThat(result.apiKey().getKeyPrefix()).startsWith("zipp_live_");
        }
    }

    @Nested
    @DisplayName("validateApiKey")
    class ValidateApiKey {

        @Test
        @DisplayName("should validate active API key")
        void shouldValidateActiveApiKey() {
            ApiKey apiKey = createValidApiKey();
            when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.of(apiKey));
            when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

            ApiKey result = apiKeyService.validateApiKey("zipp_live_testkey123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(apiKey.getId());
            verify(apiKeyRepository).save(any(ApiKey.class));
        }

        @Test
        @DisplayName("should throw exception for null API key")
        void shouldThrowExceptionForNullApiKey() {
            assertThatThrownBy(() -> apiKeyService.validateApiKey(null)).isInstanceOf(InvalidApiKeyException.class)
                    .hasMessageContaining("API key is missing");
        }

        @Test
        @DisplayName("should throw exception for blank API key")
        void shouldThrowExceptionForBlankApiKey() {
            assertThatThrownBy(() -> apiKeyService.validateApiKey("   ")).isInstanceOf(InvalidApiKeyException.class)
                    .hasMessageContaining("API key is missing");
        }

        @Test
        @DisplayName("should throw exception for invalid API key")
        void shouldThrowExceptionForInvalidApiKey() {
            when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> apiKeyService.validateApiKey("invalid_key"))
                    .isInstanceOf(InvalidApiKeyException.class).hasMessageContaining("Invalid API key");
        }

        @Test
        @DisplayName("should throw exception for revoked API key")
        void shouldThrowExceptionForRevokedApiKey() {
            ApiKey revokedKey = createValidApiKey();
            revokedKey.setStatus(KeyStatus.REVOKED);
            when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.of(revokedKey));

            assertThatThrownBy(() -> apiKeyService.validateApiKey("zipp_live_revokedkey"))
                    .isInstanceOf(InvalidApiKeyException.class).hasMessageContaining("not active or has expired");
        }

        @Test
        @DisplayName("should throw exception for expired API key")
        void shouldThrowExceptionForExpiredApiKey() {
            ApiKey expiredKey = createValidApiKey();
            expiredKey.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
            when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.of(expiredKey));

            assertThatThrownBy(() -> apiKeyService.validateApiKey("zipp_live_expiredkey"))
                    .isInstanceOf(InvalidApiKeyException.class).hasMessageContaining("not active or has expired");
        }

        @Test
        @DisplayName("should update lastUsedAt on successful validation")
        void shouldUpdateLastUsedAtOnSuccessfulValidation() {
            ApiKey apiKey = createValidApiKey();
            when(apiKeyRepository.findByKeyHash(anyString())).thenReturn(Optional.of(apiKey));
            when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

            apiKeyService.validateApiKey("zipp_live_testkey123");

            ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
            verify(apiKeyRepository).save(captor.capture());
            assertThat(captor.getValue().getLastUsedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("revokeApiKey")
    class RevokeApiKey {

        @Test
        @DisplayName("should revoke own API key")
        void shouldRevokeOwnApiKey() {
            ApiKey apiKey = createValidApiKey();
            apiKey.setUserId(userId);
            when(apiKeyRepository.findById(apiKey.getId())).thenReturn(Optional.of(apiKey));
            when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

            apiKeyService.revokeApiKey(apiKey.getId(), userId);

            ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
            verify(apiKeyRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(KeyStatus.REVOKED);
            assertThat(captor.getValue().getRevokedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when revoking another users key")
        void shouldThrowExceptionWhenRevokingAnotherUsersKey() {
            ApiKey apiKey = createValidApiKey();
            apiKey.setUserId(UUID.randomUUID());
            when(apiKeyRepository.findById(apiKey.getId())).thenReturn(Optional.of(apiKey));

            assertThatThrownBy(() -> apiKeyService.revokeApiKey(apiKey.getId(), userId))
                    .isInstanceOf(InvalidApiKeyException.class).hasMessageContaining("not allowed to revoke");

            verify(apiKeyRepository, never()).save(any(ApiKey.class));
        }

        @Test
        @DisplayName("should throw exception when API key not found")
        void shouldThrowExceptionWhenApiKeyNotFound() {
            UUID keyId = UUID.randomUUID();
            when(apiKeyRepository.findById(keyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> apiKeyService.revokeApiKey(keyId, userId))
                    .isInstanceOf(InvalidApiKeyException.class).hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("getApiKeysForUser")
    class GetApiKeysForUser {

        @Test
        @DisplayName("should return all keys for user")
        void shouldReturnAllKeysForUser() {
            List<ApiKey> keys = List.of(createValidApiKey(), createValidApiKey());
            when(apiKeyRepository.findAllByUserId(userId)).thenReturn(keys);

            List<ApiKey> result = apiKeyService.getApiKeysForUser(userId);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when no keys exist")
        void shouldReturnEmptyListWhenNoKeysExist() {
            when(apiKeyRepository.findAllByUserId(userId)).thenReturn(List.of());

            List<ApiKey> result = apiKeyService.getApiKeysForUser(userId);

            assertThat(result).isEmpty();
        }
    }

    private ApiKey createValidApiKey() {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.randomUUID());
        apiKey.setUserId(userId);
        apiKey.setKeyHash("testhash123");
        apiKey.setKeyPrefix("zipp_live_te");
        apiKey.setName("Test Key");
        apiKey.setScopes(Set.of(ApiScope.REPORTS_READ));
        apiKey.setStatus(KeyStatus.ACTIVE);
        apiKey.setCreatedAt(Instant.now());
        return apiKey;
    }
}
