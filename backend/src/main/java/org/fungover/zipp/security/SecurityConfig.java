package org.fungover.zipp.security;

import org.fungover.zipp.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ROOT = "/";
    private static final String LOGIN = "/login";
    private static final String LOGIN_ALL = "/login/**";
    private static final String OAUTH2_ALL = "/oauth2/**";
    private static final String FAVICON = "/favicon.ico";
    private static final String FAVICON_ALL = "/favicon/**";
    private static final String CSS_ALL = "/css/**";
    private static final String IMAGES_ALL = "/images/**";
    private static final String JS_ALL = "/js/**";
    private static final String WEBAUTHN_ALL = "/webauthn/**";

    private static final String WEBAUTHN_REGISTER_ALL = "/webauthn/register/**";
    private static final String WEBAUTHN_AUTHENTICATE_ALL = "/webauthn/authenticate/**";
    private static final String LOGIN_WEBAUTHN = "/login/webauthn";

    private final CustomOAuth2UserService co2us;

    @Value("${webauthn.rpId}")
    private String rpId;

    @Value("${webauthn.allowedOrigins}")
    private String allowedOrigins;

    public SecurityConfig(CustomOAuth2UserService co2us) {
        this.co2us = co2us;
    }

    @Bean
    @Order(1)
    @Profile("dev")
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(ROOT, LOGIN, LOGIN_ALL, OAUTH2_ALL, FAVICON,
                FAVICON_ALL, CSS_ALL, IMAGES_ALL, JS_ALL, WEBAUTHN_ALL).permitAll().anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2.loginPage(LOGIN).defaultSuccessUrl(ROOT, true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(co2us)))
                .webAuthn(webauthn -> webauthn.rpId(rpId).allowedOrigins(allowedOrigins.split(",")))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl(ROOT).invalidateHttpSession(true)
                        .clearAuthentication(true).deleteCookies("JSESSIONID"))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    @Order(2)
    @Profile("!dev")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(ROOT, LOGIN, LOGIN_ALL, OAUTH2_ALL, FAVICON,
                FAVICON_ALL, CSS_ALL, IMAGES_ALL, JS_ALL, WEBAUTHN_ALL).permitAll().anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.loginPage(LOGIN).defaultSuccessUrl(ROOT, true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(co2us)))
                .webAuthn(webauthn -> webauthn.rpId(rpId).allowedOrigins(allowedOrigins.split(",")))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl(ROOT).invalidateHttpSession(true)
                        .clearAuthentication(true).deleteCookies("JSESSIONID"))
                .csrf(csrf -> csrf.ignoringRequestMatchers(WEBAUTHN_REGISTER_ALL, WEBAUTHN_AUTHENTICATE_ALL,
                        LOGIN_WEBAUTHN));

        return http.build();
    }
}
