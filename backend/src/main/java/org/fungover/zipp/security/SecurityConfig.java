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
   private CustomOAuth2UserService co2us;

   public SecurityConfig(CustomOAuth2UserService co2us) {
       this.co2us = co2us;
   }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
              .requestMatchers("/").permitAll()
              .anyRequest()
              .authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
              .userInfoEndpoint(userInfo -> userInfo
                .userService(co2us))
            )
          .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)   //remove session
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")) //removes session cookie
        ;

        return http.build();

    }
}
