package org.fungover.zipp.service;

import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.exception.InvalidApiKeyException;
import org.fungover.zipp.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service-lager för all logik kring API-nycklar:
 * - generera starka API-nycklar
 * - hasha & spara dem i databasen
 * - validera inkommande API-nycklar
 * - lista & revoka nycklar för en användare
 */
@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    /**
     * Skapar en ny API-nyckel åt en användare.
     *
     * @param userId      ägarens userId
     * @param name        användarens namn på nyckeln
     * @param description frivillig beskrivning
     * @param scopes      vilka scopes nyckeln ska ha (behörigheter)
     * @param expiresAt   ev. utgångsdatum, annars null
     * @return CreatedApiKey – både plaintext nyckel (visas EN gång) och sparad entitet
     */
    @Transactional
    public CreatedApiKey createApiKey(UUID userId,
                                      String name,
                                      String description,
                                      Set<ApiKey.ApiScope> scopes,
                                      Instant expiresAt) {

        // 1) generera plaintext-nyckel
        String plainKey = generatePlainApiKey();

        // 2) hash av plaintext-nyckeln
        String keyHash = sha256Hex(plainKey);

        // 3) prefix för UI/loggar
        String keyPrefix = extractPrefix(plainKey);

        // 4) bygg entitet
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setKeyHash(keyHash);
        apiKey.setKeyPrefix(keyPrefix);
        apiKey.setName(name);
        apiKey.setDescription(description);
        apiKey.setScopes(scopes != null ? scopes : Set.of());
        apiKey.setStatus(ApiKey.KeyStatus.ACTIVE);
        apiKey.setExpiresAt(expiresAt);
        apiKey.setCreatedAt(Instant.now());

        // 5) spara i DB
        ApiKey saved = apiKeyRepository.save(apiKey);

        // 6) returnera både plaintext (till klienten) och entiteten (om man vill använda den internt)
        return new CreatedApiKey(plainKey, saved);
    }

    /**
     * Hämtar alla API-keys som tillhör en viss användare.
     */
    @Transactional(readOnly = true)
    public List<ApiKey> getApiKeysForUser(UUID userId) {
        return apiKeyRepository.findAllByUserId(userId);
    }

    /**
     * Validerar en inkommande API-nyckel (från t.ex. X-API-Key header).
     *
     * - Hashar plaintext
     * - Slår upp i DB på keyHash
     * - Kollar status/expiry via apiKey.isValid()
     * - Uppdaterar lastUsedAt
     *
     * Kastar InvalidApiKeyException om något är fel.
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
     * Revokar (stänger av) en API-nyckel.
     * Endast ägaren (userId) får revoka sin egen key.
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
    // PRIVATA HJÄLP-METODER
    // ------------------------

    /**
     * Genererar en stark random API-nyckel i plaintext.
     * Format: "zipp_live_" + base64-url-säker random-sträng.
     */
    private String generatePlainApiKey() {
        String prefix = "zipp_live_";

        byte[] randomBytes = new byte[32]; // 256 bitar
        secureRandom.nextBytes(randomBytes);

        String randomPart = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        return prefix + randomPart;
    }

    /**
     * Tar de första 12 tecknen av plaintext-keyn för att använda som prefix i UI/loggar.
     */
    private String extractPrefix(String plainKey) {
        int length = Math.min(12, plainKey.length());
        return plainKey.substring(0, length);
    }

    /**
     * Hashar input-strängen med SHA-256 och returnerar hex-sträng.
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
     * Intern record för att returnera både plaintext och entiteten
     * från createApiKey, utan att spara plaintext i databasen.
     */
    public record CreatedApiKey(String plainKey, ApiKey apiKey) {
    }
}
