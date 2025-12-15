package org.fungover.zipp.security;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByProviderId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Password is not used for WebAuthn authentication, placeholder required by
        // Spring Security
        return org.springframework.security.core.userdetails.User.withUsername(user.getProviderId()).password("{noop}")
                .roles("USER").build(); // NOSONAR
    }
}
