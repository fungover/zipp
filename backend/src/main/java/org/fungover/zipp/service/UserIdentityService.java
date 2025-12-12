package org.fungover.zipp.service;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.stereotype.Service;

@Service
public class UserIdentityService {

    private final UserRepository userRepository;

    public UserIdentityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getUserId(Authentication authentication) {
        if (authentication == null)
            throw new IllegalStateException("No authentication");
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oAuth2User) {
            Object sub = oAuth2User.getAttribute("sub");
            if (sub != null)
                return sub.toString();

            Object id = oAuth2User.getAttribute("id");
            if (id != null)
                return id.toString();

            Object userId = oAuth2User.getAttribute("user_id");
            if (userId != null)
                return userId.toString();

            throw new IllegalStateException("Could not extract user ID from OAuth2 principal");
        }

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof PublicKeyCredentialUserEntity pkUser) {
            return pkUser.getName();
        }

        return authentication.getName();
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauth) {
            String email = oauth.getAttribute("email");
            if (email == null)
                throw new IllegalStateException("OAuth2 user has no email");

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found by email: " + email));
        }

        if (principal instanceof UserDetails ud) {
            String providerId = ud.getUsername();
            return userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new IllegalStateException("User not found by providerId: " + providerId));
        }

        if (principal instanceof PublicKeyCredentialUserEntity pkUser) {
            String providerId = pkUser.getName();
            return userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new IllegalStateException("User not found by providerId: " + providerId));
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
    }
}
