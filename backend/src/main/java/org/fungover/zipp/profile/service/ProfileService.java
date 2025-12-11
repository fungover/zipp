package org.fungover.zipp.profile.service;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauth) {
            String email = oauth.getAttribute("email");
            if (email == null) {
                throw new RuntimeException("OAuth2 user has no email");
            }

            return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found by email: " + email));
        }

        String username = authentication.getName();

        return userRepository.findByProviderId(username)
            .orElseThrow(() -> new RuntimeException("User not found by providerId: " + username));
    }

    public User updateProfile(Authentication authentication, User formUser) {
        User user = getCurrentUser(authentication);

        user.setDisplayName(formUser.getDisplayName());
        user.setCity(formUser.getCity());
        user.setBio(formUser.getBio());

        return userRepository.save(user);
    }
}
