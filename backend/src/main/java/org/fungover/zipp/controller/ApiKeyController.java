package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.ApiKeyCreateRequest;
import org.fungover.zipp.dto.ApiKeyResponse;
import org.fungover.zipp.dto.ApiKeyWithSecret;
import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.service.ApiKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * REST-controller to handle API-keys for the logged-in user. Endpoints: POST
 * /api/me/api-keys -> Creates a new key (returns ApiKeyWithSecret) GET
 * /api/me/api-keys -> List all keys (returns ApiKeyResponse[]) DELETE
 * /api/me/api-keys/{id} -> Revoke a key
 */
@RestController
@RequestMapping("/api/me/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Create a new API key for the logged-in user.
     */
    @PostMapping
    public ResponseEntity<ApiKeyWithSecret> createApiKey(@Valid @RequestBody ApiKeyCreateRequest request,
            Principal principal) {
        UUID currentUserId = extractUserId(principal);

        // Calculate expiresAt based on expiresInDays (can be null)
        Instant expiresAt = null;
        if (request.expiresInDays() != null && request.expiresInDays() > 0) {
            expiresAt = Instant.now().plus(request.expiresInDays(), ChronoUnit.DAYS);
        }

        var created = apiKeyService.createApiKey(currentUserId, request.name(), request.description(), request.scopes(),
                expiresAt);

        ApiKey apiKey = created.apiKey();

        ApiKeyWithSecret response = new ApiKeyWithSecret(apiKey.getId(), apiKey.getName(), apiKey.getDescription(),
                apiKey.getKeyPrefix(), created.plainKey(), apiKey.getScopes(), apiKey.getCreatedAt(),
                apiKey.getExpiresAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all API keys that belong to the logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getMyApiKeys(Principal principal) {
        UUID currentUserId = extractUserId(principal);
        List<ApiKeyResponse> keys = apiKeyService.getApiKeysForUser(currentUserId).stream().map(this::toApiKeyResponse)
                .toList();
        return ResponseEntity.ok(keys);
    }

    /**
     * Revoke an API key that belongs to the logged-in user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeApiKey(@PathVariable UUID id, Principal principal) {
        UUID currentUserId = extractUserId(principal);
        apiKeyService.revokeApiKey(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------
    // Helper methods
    // ------------------------

    private ApiKeyResponse toApiKeyResponse(ApiKey apiKey) {
        return new ApiKeyResponse(apiKey.getId(), apiKey.getName(), apiKey.getDescription(), apiKey.getKeyPrefix(),
                apiKey.getScopes(), apiKey.getStatus(), apiKey.getCreatedAt(), apiKey.getLastUsedAt(),
                apiKey.getExpiresAt(), apiKey.getRevokedAt());
    }

    private UUID extractUserId(Principal principal) {
        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Could not extract userId from principal: " + principal.getName(), e);
        }
    }
}
