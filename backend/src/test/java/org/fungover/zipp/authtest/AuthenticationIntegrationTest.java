package org.fungover.zipp.authtest;

import java.util.List;
import java.util.Map;

import org.fungover.zipp.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testNotLoggedInUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/me")).andExpect(status().is3xxRedirection());
    }

    @Test
    void testPublicEndpointDoesNotRequiredAuth() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void testLoggedInUser() throws Exception {
        OAuth2User mockUser = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("USER")),
                Map.of("sub", "mock-google-id-12345", "name", "Mock User", "email", "mock@example.com"), "sub");

        mockMvc.perform(get("/me").with(oauth2Login().oauth2User(mockUser))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello Mock User")))
                .andExpect(content().string(containsString("mock@example.com")));
    }

    @Test
    void testIndexPageWhenLoggedIn() throws Exception {

        final String expectedUserName = "Test User Name";

        OAuth2User mockUser = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("USER")),
                Map.of("sub", "test-id-9876", "name", expectedUserName, "email", "test@example.com"), "sub");

        mockMvc.perform(get("/").with(oauth2Login().oauth2User(mockUser))).andExpect(status().isOk())
                .andExpect(model().attribute("isLoggedIn", true))
                .andExpect(model().attribute("userName", expectedUserName))
                .andExpect(model().attribute("title", "Zipp"));
    }
}
