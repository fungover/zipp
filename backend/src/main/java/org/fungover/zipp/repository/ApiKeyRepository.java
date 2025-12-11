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
     * Hitta en API-nyckel baserat på dess hash.
     * Används vid varje API-anrop för att validera nyckeln.
     */
    Optional<ApiKey> findByKeyHash(String keyHash);

    /**
     * Lista alla API-nycklar för en användare, nyaste först.
     * Används i "Hantera API-nycklar" i användargränssnittet.
     */
    List<ApiKey> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Lista nycklar med en specifik status för en användare.
     */
    List<ApiKey> findByUserIdAndStatus(UUID userId, KeyStatus status);

    /**
     * Hitta en specifik nyckel som tillhör en specifik användare.
     * Säkerställer att användare bara kan se/ändra sina egna nycklar.
     */
    Optional<ApiKey> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Kontrollera om en nyckel-hash redan finns (för att undvika kollisioner).
     */
    boolean existsByKeyHash(String keyHash);

    /**
     * Uppdatera lastUsedAt utan att ladda hela entiteten.
     * Körs vid varje lyckat API-anrop.
     */
    @Modifying
    @Query("UPDATE ApiKey a SET a.lastUsedAt = :timestamp WHERE a.id = :id")
    void updateLastUsedAt(@Param("id") UUID id, @Param("timestamp") Instant timestamp);

    /**
     * Markera utgångna nycklar som EXPIRED.
     * Körs av en schemalagd bakgrundsprocess.
     */
    @Modifying
    @Query("UPDATE ApiKey a SET a.status = 'EXPIRED' WHERE a.expiresAt < :now AND a.status = 'ACTIVE'")
    int expireOldKeys(@Param("now") Instant now);

    /**
     * Räkna antal aktiva nycklar för en användare.
     * Används för att begränsa antal nycklar per användare (max 10).
     */
    @Query("SELECT COUNT(a) FROM ApiKey a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    long countActiveKeysByUserId(@Param("userId") UUID userId);
}
