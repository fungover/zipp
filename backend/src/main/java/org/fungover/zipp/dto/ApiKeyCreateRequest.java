package org.fungover.zipp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import org.fungover.zipp.entity.ApiKey.ApiScope;

import java.util.Set;

/**
 * Request to create a new API key.
 *
 * @param name
 *            user-defined name for the key
 * @param description
 *            optional description
 * @param scopes
 *            permissions granted to the key
 * @param expiresInDays
 *            days until expiration, null for no expiration
 */
public record ApiKeyCreateRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters") String name,

        @Size(max = 500, message = "Description must be at most 500 chars") String description,

        Set<ApiScope> scopes,

        @Min(value = 1, message = "Expiration must be at least 1 day") Integer expiresInDays

) {
    /**
     * Compact constructor - sets default scopes if none provided.
     */
    public ApiKeyCreateRequest {
        if (scopes == null || scopes.isEmpty()) {
            scopes = Set.of(ApiScope.REPORTS_READ, ApiScope.CATEGORIES_READ);
        }
    }
}
