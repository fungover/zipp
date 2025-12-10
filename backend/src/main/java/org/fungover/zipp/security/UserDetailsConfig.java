package org.fungover.zipp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        // Dummy-user för WebAuthn (krävs för bootstrap)
        return new InMemoryUserDetailsManager(
            User.withUsername("dummy")
                .password("{noop}dummy")
                .roles("USER")
                .build()
        );
    }
}
