package org.fungover.zipp.security;

import org.fungover.zipp.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService co2us;

    public SecurityConfig(CustomOAuth2UserService co2us) {
        this.co2us = co2us;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login",
                    "/favicon.ico", "/favicon/**",
                    "/css/**", "/js/**",
                    "/images/**", "/img/**",
                    "/webauthn/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(userInfo -> userInfo.userService(co2us))
            )
            .webAuthn(webauthn -> webauthn
                .rpId("localhost")
                .allowedOrigins("http://localhost:8080")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/webauthn/register/**",
                    "/webauthn/authenticate/**"
                )
            );

        return http.build();
    }
}
