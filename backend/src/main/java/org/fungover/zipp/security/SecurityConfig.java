package org.fungover.zipp.security;

import org.fungover.zipp.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomOAuth2UserService co2us;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Value("${zipp.security.graphql-open:false}")
    private boolean graphqlOpen;

    public SecurityConfig(CustomOAuth2UserService co2us,
                          ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
        this.co2us = co2us;
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
    }

    /**
     * Security filter chain for M2M API (API key authentication).
     * Runs first for /graphql and /api/m2m/** endpoints.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiKeySecurityChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/graphql/**", "/api/m2m/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> {
                if (graphqlOpen) {
                    auth.requestMatchers("/graphql/**").permitAll();
                } else {
                    auth.requestMatchers("/graphql/**").hasRole("API_CLIENT");
                }
                auth.requestMatchers("/api/m2m/**").hasRole("API_CLIENT")
                    .anyRequest().authenticated();
            })
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"error\": \"Unauthorized\", \"message\": \"API key required. " +
                            "Include X-API-Key header with a valid key.\"}"
                    );
                })
            );

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/favicon.ico", "/favicon/**", "/css/**", "/images/**", "/js/**")
                .permitAll().anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.loginPage("/login").defaultSuccessUrl("/", true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(co2us)))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").invalidateHttpSession(true)
                        .clearAuthentication(true).deleteCookies("JSESSIONID"));
        return http.build();
    }

    /**
     * Security filter chain for regular users (OAuth2).
     * Handles all other endpoints.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain oauthSecurityChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/keys/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/favicon.ico", "/favicon/**", "/css/**", "/images/**", "/js/**")
                .permitAll()
                .requestMatchers("/graphiql/**").permitAll()
                .requestMatchers("/api/keys/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(userInfo -> userInfo.userService(co2us))
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
