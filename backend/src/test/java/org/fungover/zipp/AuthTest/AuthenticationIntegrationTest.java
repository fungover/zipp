package org.fungover.zipp.AuthTest;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testNotLoggedInUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/me"))
            .andExpect((status().is3xxRedirection()));
    }

    @Test
    void testPublicEndpointDoesNotRequiredAuth() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect((status().isOk()));
    }

    @Test
    void testLoggedInUser() throws Exception {
        OAuth2User mockUser = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("USER")),
            Map.of(
                "sub", "mock-google-id-12345",
                "name", "Mock User",
                "email", "mock@example.com"
            ),
            "sub"
        );

        mockMvc.perform(get("/me")
                            .with(oauth2Login().oauth2User(mockUser)))
               .andExpect(status().isOk())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello Mock User")))
               .andExpect(content().string(org.hamcrest.Matchers.containsString("mock@example.com")));
    }
}


