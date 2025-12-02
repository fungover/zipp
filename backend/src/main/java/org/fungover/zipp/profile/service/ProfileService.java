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
        OAuth2User oauth = (OAuth2User) authentication.getPrincipal();
        String email = oauth.getAttribute("email");

        return userRepository.findUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found in DB"));
    }

    public User updateProfile(Authentication authentication, User formUser) {
        User user = getCurrentUser(authentication);

        user.setDisplayName(formUser.getDisplayName());
        user.setCity(formUser.getCity());
        user.setBio(formUser.getBio());

        return userRepository.save(user);
    }
}
