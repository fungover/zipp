package org.fungover.zipp.dto;

import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.entity.ApiKey.KeyStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ApiKeyResponse(
    UUID id,
    String name,
    String description,
    String keyPrefix,
    Set<ApiScope> scopes,
    KeyStatus status,
    Instant createdAt,
    Instant lastUsedAt,
    Instant expiresAt,
    Instant revokedAt
) {}
