package org.fungover.zipp.security;

import org.fungover.zipp.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService co2us;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;


    public SecurityConfig(CustomOAuth2UserService co2us,
                          ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
        this.co2us = co2us;
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Add our API key filter before the default UsernamePasswordAuthenticationFilter
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            .authorizeHttpRequests(auth -> auth
                // public
                .requestMatchers("/", "/login", "/favicon.ico", "/favicon/**", "/css/**", "/images/**", "/js/**")
                .permitAll()

                // M2M / GraphQL: requires authentication – can be done via the API key filter
                .requestMatchers("/api/m2m/**", "/graphql")
                .hasRole("API_CLIENT")

                // the rest requires regular user login (OAuth2)
                .anyRequest()
                .authenticated()
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
