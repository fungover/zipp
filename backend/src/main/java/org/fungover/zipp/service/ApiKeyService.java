package org.fungover.zipp.service;

import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.exception.InvalidApiKeyException;
import org.fungover.zipp.repository.ApiKeyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service layer for all logic around API keys: - generate strong API keys -
 * hash and save them in the database - validate incoming API keys - list &
 * revoke keys for a user
 */
@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    /**
     * Creates a new API key for a user.
     *
     * @param userId
     *            owner's userId
     * @param name
     *            user name on the key
     * @param description
     *            optional description
     * @param scopes
     *            set of API scopes defining access permissions
     * @param expiresAt
     *            possible expiration date, otherwise null
     * @return CreatedApiKey – both plaintext key (shown ONCE) and saved entity
     */
    @Transactional
    public CreatedApiKey createApiKey(UUID userId, String name, String description, Set<ApiKey.ApiScope> scopes,
            Instant expiresAt) {

        long activeKeys = apiKeyRepository.countActiveKeysByUserId(userId);
        if (activeKeys >= 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum number of active API keys reached (10)");
        }

        // 1) generate plaintext key
        String plainKey = generatePlainApiKey();

        // 2) hash of the plaintext key
        String keyHash = sha256Hex(plainKey);

        // 3) prefix for UI/logs
        String keyPrefix = extractPrefix(plainKey);

        // 4) build entity
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setKeyHash(keyHash);
        apiKey.setKeyPrefix(keyPrefix);
        apiKey.setName(name);
        apiKey.setDescription(description);
        apiKey.setScopes(scopes != null ? scopes : Set.of());
        apiKey.setStatus(ApiKey.KeyStatus.ACTIVE);
        apiKey.setExpiresAt(expiresAt);
        // apiKey.setCreatedAt(Instant.now()); I don´t believe this is needed, since
        // java does it automatically thanks to @PrePersist in the ApiKey entity

        // 5) save in DB
        ApiKey saved = apiKeyRepository.save(apiKey);

        // 6) return both plaintext (to the client) and the entity (if you want to use
        // it internally)
        return new CreatedApiKey(plainKey, saved);
    }

    /**
     * Retrieves all API keys belonging to a specific user.
     */
    @Transactional(readOnly = true)
    public List<ApiKey> getApiKeysForUser(UUID userId) {
        return apiKeyRepository.findAllByUserId(userId);
    }

    /**
     * Validates an incoming API key (from e.g. X-API-Key header).
     *
     * - Hash plaintext - Looking up in DB on keyHash - Checking status/expiry via
     * apiKey.isValid() - Updating lastUsedAt
     *
     */
    @Transactional
    public ApiKey validateApiKey(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            throw new InvalidApiKeyException("API key is missing");
        }

        String hash = sha256Hex(rawApiKey);

        ApiKey apiKey = apiKeyRepository.findByKeyHash(hash)
                .orElseThrow(() -> new InvalidApiKeyException("Invalid API key"));

        if (!apiKey.isValid()) {
            throw new InvalidApiKeyException("API key is not active or has expired");
        }

        apiKey.setLastUsedAt(Instant.now());
        apiKeyRepository.save(apiKey);

        return apiKey;
    }

    /**
     * Revokes (turns off) an API key. Only the owner (userId) may revoke their own
     * key.
     */
    @Transactional
    public void revokeApiKey(UUID apiKeyId, UUID currentUserId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new InvalidApiKeyException("API key not found"));

        if (!apiKey.getUserId().equals(currentUserId)) {
            throw new InvalidApiKeyException("You are not allowed to revoke this API key");
        }

        apiKey.setStatus(ApiKey.KeyStatus.REVOKED);
        apiKey.setRevokedAt(Instant.now());
        apiKeyRepository.save(apiKey);
    }

    // ------------------------
    // PRIVAT HELPER METODS
    // ------------------------

    /**
     * Generates a strong random API key in plaintext. Format: "zipp_live_" +
     * base64-url-safe random string.
     */
    private String generatePlainApiKey() {
        String prefix = "zipp_live_";

        byte[] randomBytes = new byte[32]; // 256 bitar
        secureRandom.nextBytes(randomBytes);

        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        return prefix + randomPart;
    }

    /**
     * Takes the first 12 characters of the plaintext key to use as a prefix in
     * UI/logs.
     */
    private String extractPrefix(String plainKey) {
        int length = Math.min(12, plainKey.length());
        return plainKey.substring(0, length);
    }

    /**
     * Hash the input string with SHA-256 and returns a hex string.
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new IllegalStateException("Could not hash API key", e);
        }
    }

    /**
     * Internal record to return both plaintext and the entity from createApiKey,
     * without saving plaintext in the database.
     */
    public record CreatedApiKey(String plainKey, ApiKey apiKey) {
    }
}
