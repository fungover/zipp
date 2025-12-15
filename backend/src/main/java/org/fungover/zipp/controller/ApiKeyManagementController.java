package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.ApiKeyCreateRequest;
import org.fungover.zipp.dto.ApiKeyResponse;
import org.fungover.zipp.dto.ApiKeyWithSecret;
import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.exception.InvalidApiKeyException;
import org.fungover.zipp.service.ApiKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for API key management. Requires OAuth2 authentication (user
 * must be logged in via Google).
 */
@RestController
@RequestMapping("/api/keys")
public class ApiKeyManagementController {

    private final ApiKeyService apiKeyService;

    public ApiKeyManagementController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Create a new API key. The secret is returned only in this response and cannot
     * be retrieved again.
     */
    @PostMapping
    public ResponseEntity<ApiKeyWithSecret> createApiKey(@AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody ApiKeyCreateRequest request) {

        UUID userId = extractUserId(principal);

        Instant expiresAt = null;
        if (request.expiresInDays() != null) {
            expiresAt = Instant.now().plusSeconds(request.expiresInDays() * 86400L);
        }

        ApiKeyService.CreatedApiKey created = apiKeyService.createApiKey(userId, request.name(), request.description(),
                request.scopes(), expiresAt);

        ApiKeyWithSecret response = new ApiKeyWithSecret(created.apiKey().getId(), created.apiKey().getName(),
                created.apiKey().getDescription(), created.apiKey().getKeyPrefix(), created.plainKey(),
                created.apiKey().getScopes(), created.apiKey().getCreatedAt(), created.apiKey().getExpiresAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all API keys for the current user. Does not include the secret keys.
     */
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys(@AuthenticationPrincipal OAuth2User principal) {

        UUID userId = extractUserId(principal);
        List<ApiKey> keys = apiKeyService.getApiKeysForUser(userId);

        List<ApiKeyResponse> response = keys.stream().map(this::toResponse).toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get details of a specific API key.
     */
    @GetMapping("/{keyId}")
    public ResponseEntity<ApiKeyResponse> getApiKey(@AuthenticationPrincipal OAuth2User principal,
            @PathVariable UUID keyId) {

        UUID userId = extractUserId(principal);
        List<ApiKey> keys = apiKeyService.getApiKeysForUser(userId);

        ApiKey key = keys.stream().filter(k -> k.getId().equals(keyId)).findFirst()
                .orElseThrow(() -> new InvalidApiKeyException("API key not found"));

        return ResponseEntity.ok(toResponse(key));
    }

    /**
     * Revoke an API key.
     */
    @DeleteMapping("/{keyId}")
    public ResponseEntity<Map<String, String>> revokeApiKey(@AuthenticationPrincipal OAuth2User principal,
            @PathVariable UUID keyId) {

        UUID userId = extractUserId(principal);
        apiKeyService.revokeApiKey(keyId, userId);

        return ResponseEntity.ok(Map.of("message", "API key revoked successfully"));
    }

    /**
     * Get available API scopes with descriptions.
     */
    @GetMapping("/scopes")
    public ResponseEntity<List<ScopeInfo>> getAvailableScopes() {
        List<ScopeInfo> scopes = Arrays.stream(ApiKey.ApiScope.values())
                .map(scope -> new ScopeInfo(scope.name(), getScopeDescription(scope))).toList();

        return ResponseEntity.ok(scopes);
    }

    private UUID extractUserId(OAuth2User principal) {
        String sub = principal.getAttribute("sub");
        if (sub != null) {
            return UUID.nameUUIDFromBytes(sub.getBytes());
        }

        Object idAttr = principal.getAttribute("id");
        if (idAttr != null) {
            if (idAttr instanceof UUID uuid) {
                return uuid;
            }
            return UUID.fromString(idAttr.toString());
        }

        String email = principal.getAttribute("email");
        if (email != null) {
            return UUID.nameUUIDFromBytes(email.getBytes());
        }

        throw new IllegalStateException("Cannot extract user ID from principal");
    }

    private ApiKeyResponse toResponse(ApiKey apiKey) {
        return new ApiKeyResponse(apiKey.getId(), apiKey.getName(), apiKey.getDescription(), apiKey.getKeyPrefix(),
                apiKey.getScopes(), apiKey.getStatus(), apiKey.getCreatedAt(), apiKey.getLastUsedAt(),
                apiKey.getExpiresAt(), apiKey.getRevokedAt());
    }

    private String getScopeDescription(ApiKey.ApiScope scope) {
        return switch (scope) {
            case REPORTS_READ -> "Read location reports";
            case REPORTS_WRITE -> "Create new location reports";
            case REPORTS_DELETE -> "Delete location reports";
            case CATEGORIES_READ -> "Read report categories";
            case USERS_READ -> "Read limited user information";
            case STATS_READ -> "Read statistics and analytics";
        };
    }

    record ScopeInfo(String name, String description) {
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<Map<String, String>> handleApiKeyException(InvalidApiKeyException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
