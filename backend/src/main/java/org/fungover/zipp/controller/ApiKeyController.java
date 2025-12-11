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
 * REST-controller för att hantera API-nycklar för den inloggade användaren.
 *
 * Endpoints:
 *  POST   /api/me/api-keys       -> skapa ny key (returnerar ApiKeyWithSecret)
 *  GET    /api/me/api-keys       -> lista alla mina keys (returnerar ApiKeyResponse[])
 *  DELETE /api/me/api-keys/{id}  -> revoka en key
 */
@RestController
@RequestMapping("/api/me/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * Skapa en ny API-nyckel åt den inloggade användaren.
     *
     * Body: ApiKeyCreateRequest (name, description, scopes, expiresInDays)
     * Return: ApiKeyWithSecret (id, namn, prefix, secretKey, scopes, createdAt, expiresAt)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyWithSecret createApiKey(@Valid @RequestBody ApiKeyCreateRequest request,
                                         Principal principal) {

        UUID currentUserId = extractUserId(principal);

        // Räkna ut expiresAt baserat på expiresInDays (kan vara null)
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

        // ApiKeyWithSecret = det du visar EN gång efter skapandet
        return new ApiKeyWithSecret(
            apiKey.getId(),
            apiKey.getName(),
            apiKey.getDescription(),
            apiKey.getKeyPrefix(),
            created.plainKey(),       // ⚠️ plaintext secret, bara här!
            apiKey.getScopes(),
            apiKey.getCreatedAt(),
            apiKey.getExpiresAt()
        );
    }

    /**
     * Lista alla API-keys som tillhör den inloggade användaren.
     * Returnerar INTE secretKey – bara metadata.
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
     * Revoka (stäng av) en API-nyckel som tillhör den inloggade användaren.
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
     * Mappa entiteten ApiKey -> ApiKeyResponse (utan secret).
     * Används vid listning.
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
     * Plocka ut userId (UUID) från Principal.
     *
     * JUSTERA DEN HÄR så den matchar hur ni lagrar användarens ID:
     *  - Om principal.getName() ÄR ett UUID-string -> funkar direkt.
     *  - Om ni har en custom User/Principal-typ -> byt implementation.
     */
    private UUID extractUserId(Principal principal) {
        try {
            return UUID.fromString(principal.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Could not extract userId from principal: " + principal.getName(), e);
        }
    }
}
