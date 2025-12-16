package org.fungover.zipp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "api_keys", indexes = {@Index(name = "idx_api_key_hash", columnList = "keyHash"),
        @Index(name = "idx_api_key_user", columnList = "userId")})
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false, length = 12)
    private String keyPrefix;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_scopes", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    private Set<ApiScope> scopes = EnumSet.noneOf(ApiScope.class);

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
        createdAt = Instant.now();
    }

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
        return scopes.contains(scope);
    }

    public enum KeyStatus {
        ACTIVE, REVOKED, EXPIRED
    }

    /**
     * API scopes control what operations an API key can perform.
     */
    public enum ApiScope {
        REPORTS_READ, REPORTS_WRITE, REPORTS_DELETE, CATEGORIES_READ, USERS_READ, STATS_READ
    }
}
