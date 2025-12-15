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

    private static final String USER_NOT_FOUND_BY_PROVIDER_ID = "User not found by providerId: ";

    public String getUserId(Authentication authentication) {

        if (authentication == null) {
            throw new IllegalStateException("No authentication");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oAuth2User) {
            return extractOAuth2UserId(oAuth2User);
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
            if (email != null && !email.isBlank()) {
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalStateException("User not found by email: " + email));
            }

            String providerId = extractOAuth2UserId(oauth);
            return userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new IllegalStateException(USER_NOT_FOUND_BY_PROVIDER_ID + providerId));
        }

        if (principal instanceof UserDetails userDetails) {
            String providerId = userDetails.getUsername();
            return userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new IllegalStateException(USER_NOT_FOUND_BY_PROVIDER_ID + providerId));
        }

        if (principal instanceof PublicKeyCredentialUserEntity pkUser) {
            String providerId = pkUser.getName();
            return userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new IllegalStateException(USER_NOT_FOUND_BY_PROVIDER_ID + providerId));
        }

        if (principal instanceof User user) {
            return user;
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
    }

    private String extractOAuth2UserId(OAuth2User oAuth2User) {
        Object sub = oAuth2User.getAttribute("sub");
        if (sub != null) {
            return sub.toString();
        }

        Object id = oAuth2User.getAttribute("id");
        if (id != null) {
            return id.toString();
        }

        Object userId = oAuth2User.getAttribute("user_id");
        if (userId != null) {
            return userId.toString();
        }

        throw new IllegalStateException("Could not extract user ID from OAuth2 principal");
    }
}
