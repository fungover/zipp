package org.fungover.zipp.dto;

import org.fungover.zipp.entity.ApiKey.ApiScope;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ApiKeyWithSecret(
    UUID id,
    String name,
    String description,
    String keyPrefix,
    String secretKey,  // ⚠️ Visas endast här - kan aldrig hämtas igen!
    Set<ApiScope> scopes,
    Instant createdAt,
    Instant expiresAt
) {}
