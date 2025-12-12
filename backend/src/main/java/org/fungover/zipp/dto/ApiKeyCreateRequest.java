package org.fungover.zipp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.fungover.zipp.entity.ApiKey.ApiScope;

import java.util.Set;

public record ApiKeyCreateRequest(

        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,

        @Size(max = 500, message = "Description must be at most 500 characters") String description,

        Set<ApiScope> scopes,

        Integer expiresInDays

) {
    public ApiKeyCreateRequest {
        if (scopes == null || scopes.isEmpty()) {
            scopes = Set.of(ApiScope.REPORTS_READ, ApiScope.CATEGORIES_READ);
        }
    }
}
