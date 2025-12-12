package org.fungover.zipp.dto;

import org.fungover.zipp.entity.ApiKey.ApiScope;

import java.util.Set;
import java.util.UUID;

/**
 * API key validation results.
 */
public record ApiKeyValidationResult(boolean valid, UUID userId, UUID keyId, Set<ApiScope> scopes,
        String errorMessage) {
    public static ApiKeyValidationResult valid(UUID userId, UUID keyId, Set<ApiScope> scopes) {
        return new ApiKeyValidationResult(true, userId, keyId, scopes, null);
    }

    public static ApiKeyValidationResult invalid(String message) {
        return new ApiKeyValidationResult(false, null, null, Set.of(), message);
    }

    public boolean hasScope(ApiScope scope) {
        return scopes != null && scopes.contains(scope);
    }
}
