package org.fungover.zipp.repository;

import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.entity.ApiKey.KeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    /**
     * Find API key based on hash code. Is used every API call to validate key
     */
    Optional<ApiKey> findByKeyHash(String keyHash);

    /**
     * List all keys for users - newest first Is used in the UI for key handling
     */
    List<ApiKey> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * List keys with specific status for a user
     */
    List<ApiKey> findByUserIdAndStatus(UUID userId, KeyStatus status);

    /**
     * Find a specific key - for a specific user Ensures key privacy
     */
    Optional<ApiKey> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Control if there is already an existent key hash
     */
    boolean existsByKeyHash(String keyHash);

    /**
     * Update lastUsedAt in isolation Runs with every successful API call
     */
    @Modifying
    @Query("UPDATE ApiKey a SET a.lastUsedAt = :timestamp WHERE a.id = :id")
    void updateLastUsedAt(@Param("id") UUID id, @Param("timestamp") Instant timestamp);

    /**
     * Mark expired keys as expired A schedueled background process
     */
    @Modifying
    @Query("UPDATE ApiKey a SET a.status = 'EXPIRED' WHERE a.expiresAt < :now AND a.status = 'ACTIVE'")
    int expireOldKeys(@Param("now") Instant now);

    /**
     * Count amount of active keys for a user. Is used to limit amount of keys per
     * user (max 10)
     */
    @Query("SELECT COUNT(a) FROM ApiKey a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    long countActiveKeysByUserId(@Param("userId") UUID userId);

    List<ApiKey> findAllByUserId(UUID userId);
}
