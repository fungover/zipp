package org.fungover.zipp.controller;

import jakarta.validation.Valid;
import org.fungover.zipp.dto.ApiKeyCreateRequest;
import org.fungover.zipp.dto.ApiKeyResponse;
import org.fungover.zipp.dto.ApiKeyWithSecret;
import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.service.ApiKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * REST-controller to handle API-keys for the logged in user.
 * Endpoints:
 *  POST   /api/me/api-keys       -> Creates a new key (returns ApiKeyWithSecret)
 *  GET    /api/me/api-keys       -> list all keys (returns ApiKeyResponse[])
 *  DELETE /api/me/api-keys/{id}  -> revoke a key
 */
@RestController
@RequestMapping("/api/me/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Create a new API-key for the logged-in user.
     * Body: ApiKeyCreateRequest (name, description, scopes, expiresInDays)
     * Return: ApiKeyWithSecret (id, namn, prefix, secretKey, scopes, createdAt, expiresAt)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyWithSecret createApiKey(@Valid @RequestBody ApiKeyCreateRequest request,
                                         Principal principal) {

        UUID currentUserId = extractUserId(principal);

        // Calculate expiresAt based on expiresInDays (can be null)
        Instant expiresAt = null;
        if (request.expiresInDays() != null && request.expiresInDays() > 0) {
            expiresAt = Instant.now().plus(request.expiresInDays(), ChronoUnit.DAYS);
        }

        var created = apiKeyService.createApiKey(
            currentUserId,
            request.name(),
            request.description(),
            request.scopes(),
            expiresAt
        );

        ApiKey apiKey = created.apiKey();

        // ApiKeyWithSecret = what is shown only once post creation
        return new ApiKeyWithSecret(
            apiKey.getId(),
            apiKey.getName(),
            apiKey.getDescription(),
            apiKey.getKeyPrefix(),
            created.plainKey(),
            apiKey.getScopes(),
            apiKey.getCreatedAt(),
            apiKey.getExpiresAt()
        );
    }

    /**
     * Lists all API-keys that belongs to the logged-in user
     * Returns NOT secretKey - ONLY metadata.
     */
    @GetMapping
    public List<ApiKeyResponse> getMyApiKeys(Principal principal) {
        UUID currentUserId = extractUserId(principal);

        return apiKeyService.getApiKeysForUser(currentUserId)
            .stream()
            .map(this::toApiKeyResponse)
            .toList();
    }

    /**
     * Revoke an API-key that belongs the logged-in user.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeApiKey(@PathVariable UUID id, Principal principal) {
        UUID currentUserId = extractUserId(principal);
        apiKeyService.revokeApiKey(id, currentUserId);
    }

    // ------------------------
    //   HJÄLP-METODER
    // ------------------------

    /**
     * Map the entity ApiKey -> ApiKeyResponse (without secret).
     */

    private ApiKeyResponse toApiKeyResponse(ApiKey apiKey) {
        return new ApiKeyResponse(
            apiKey.getId(),
            apiKey.getName(),
            apiKey.getDescription(),
            apiKey.getKeyPrefix(),
            apiKey.getScopes(),
            apiKey.getStatus(),
            apiKey.getCreatedAt(),
            apiKey.getLastUsedAt(),
            apiKey.getExpiresAt(),
            apiKey.getRevokedAt()
        );
    }

    /**
     * Cherrypick userId (UUID) from Principal.
     * ADJUST IT HERE TO MATCH USERS ID.
     *  - If principal.getName() is a UUID-string -> works directly.
     *  - If custom User/Principal-type -> change implementation.
     */
    private UUID extractUserId(Principal principal) {
        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Could not extract userId from principal: " + principal.getName(), e);
        }
    }
}
