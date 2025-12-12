package org.fungover.zipp.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This is supposed to use User.
 * This is the actual key. This will behold the model for an API-key in the db
 **/
@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_api_key_hash", columnList = "keyHash"),
    @Index(name = "idx_api_key_user", columnList = "userId")
})
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;  // References the user with passkey auth

    @Column(nullable = false, unique = true)
    private String keyHash;  // SHA-256 hash of the actual key

    @Column(nullable = false, length = 12)
    private String keyPrefix;  // First 12 chars for identification (e.g., "zipp_live_ab")

    @Column(nullable = false, length = 100)
    private String name;  // User-defined name for the key

    @Column(length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_scopes", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    private Set<ApiScope> scopes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KeyStatus status = KeyStatus.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant lastUsedAt;

    private Instant expiresAt;

    private Instant revokedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // Constructors
    public ApiKey() {
    }

    public ApiKey(UUID userId, String keyHash, String keyPrefix, String name) {
        this.userId = userId;
        this.keyHash = keyHash;
        this.keyPrefix = keyPrefix;
        this.name = name;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ApiScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<ApiScope> scopes) {
        this.scopes = scopes;
    }

    public KeyStatus getStatus() {
        return status;
    }

    public void setStatus(KeyStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public boolean isValid() {
        if (status != KeyStatus.ACTIVE) {
            return false;
        }
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    public boolean hasScope(ApiScope scope) {
        return scopes != null && scopes.contains(scope);
    }

    public enum KeyStatus {
        ACTIVE,
        REVOKED,
        EXPIRED
    }

    /**
     * API scopes control what operations an API key can perform.
     */
    public enum ApiScope {
        REPORTS_READ,      // Read location reports
        REPORTS_WRITE,     // Create new reports
        REPORTS_DELETE,    // Delete reports
        CATEGORIES_READ,   // Read report categories
        USERS_READ,        // Read user info (limited)
        STATS_READ         // Read statistics
    }
}
