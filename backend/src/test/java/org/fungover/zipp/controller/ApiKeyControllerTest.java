package org.fungover.zipp.controller;

import org.fungover.zipp.entity.ApiKey;
import org.fungover.zipp.entity.ApiKey.ApiScope;
import org.fungover.zipp.dto.ApiKeyCreateRequest;
import org.fungover.zipp.dto.ApiKeyWithSecret;
import org.fungover.zipp.dto.ApiKeyResponse;
import org.fungover.zipp.service.ApiKeyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiKeyService apiKeyService;

    @Test
    void testGetMyApiKeys() throws Exception {
        UUID userId = UUID.randomUUID();
        ApiKey apiKey = new ApiKey(userId, "MyKey", "Test key", "MK");
        apiKey.setScopes(Set.of(ApiScope.READ, ApiScope.WRITE));

        when(apiKeyService.getApiKeysForUser(any()))
            .thenReturn(java.util.List.of(apiKey));

        OAuth2User mockUser = new DefaultOAuth2User(
            java.util.List.of(new SimpleGrantedAuthority("USER")),
            java.util.Map.of("sub", userId.toString(), "name", "Test User", "email", "test@example.com"),
            "sub");

        mockMvc.perform(get("/api/me/api-keys").with(oauth2Login().oauth2User(mockUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("MyKey"))
            .andExpect(jsonPath("$[0].keyPrefix").value("MK"));
    }

    @Test
    void testCreateApiKey() throws Exception {
        UUID userId = UUID.randomUUID();
        ApiKey apiKey = new ApiKey(userId, "NewKey", "Description", "NK");
        apiKey.setScopes(Set.of(ApiScope.READ, ApiScope.WRITE));

        ApiKeyWithSecret createdKey = new ApiKeyWithSecret(apiKey, "SECRET-123");

        when(apiKeyService.createApiKey(any(), any(), any(), any(), any()))
            .thenReturn(createdKey);

        OAuth2User mockUser = new DefaultOAuth2User(
            java.util.List.of(new SimpleGrantedAuthority("USER")),
            java.util.Map.of("sub", userId.toString(), "name", "Test User", "email", "test@example.com"),
            "sub");

        String requestBody = """
                {
                    "name": "NewKey",
                    "description": "Description",
                    "scopes": ["READ", "WRITE"],
                    "expiresInDays": 30
                }
                """;

        mockMvc.perform(post("/api/me/api-keys")
                .with(oauth2Login().oauth2User(mockUser))
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("NewKey"))
            .andExpect(jsonPath("$.secretKey").value("SECRET-123"));
    }
}
