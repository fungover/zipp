package org.fungover.zipp.security;

import org.fungover.zipp.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("dev")
public class SecurityConfig {

    private final CustomOAuth2UserService co2us;

    public SecurityConfig(CustomOAuth2UserService co2us) {
        this.co2us = co2us;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withUsername("dummy")
                .password("{noop}dummy")
                .roles("USER")
                .build()
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/webauthn/**",
                    "/css/**", "/js/**", "/img/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
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

@Configuration
@EnableWebSecurity
@Profile("prod")
public class ProdSecurityConfig {

    private final CustomOAuth2UserService co2us;

    public ProdSecurityConfig(CustomOAuth2UserService co2us) {
        this.co2us = co2us;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/webauthn/**", "/css/**", "/js/**", "/img/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(co2us))
            )
            .webAuthn(webauthn -> webauthn
                // ÄNDRA DETTA TILL DIN PRODUKTIONSDOMÄN
                .rpId("minriktigadomän.se")
                .allowedOrigins("https://minriktigadomän.se")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/webauthn/register/**", "/webauthn/authenticate/**")
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                            "script-src 'self'; " +
                            "style-src 'self' 'unsafe-inline'; " +
                            "img-src 'self' data:; " +
                            "connect-src 'self'; " +
                            "frame-ancestors 'none';"
                    )
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                .xssProtection(xss -> xss.block(true))
                .frameOptions(frame -> frame.deny())
            )
            .requiresChannel(channel ->
                channel.anyRequest().requiresSecure()
            );

        return http.build();
    }
}

