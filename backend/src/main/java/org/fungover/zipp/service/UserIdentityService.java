package org.fungover.zipp.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class UserIdentityService {
    /**
     * Extracts the user {@code ID} from the authentication object.
     * If it doesn't find one, it throws an exception.
     * If the principal is an {@link OAuth2User}, it tries to extract the user ID from the attributes: {@code sub}, {@code id}, {@code user_id}
     * Currently, this only manages {@code OAuth2 authentication} but can be built further on.
     * @param authentication
     * @return the extracted {@code ID} as a {@code String}
     */
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

        return authentication.getName();
    }

    /**
     * This method attempts to extract the user {@code ID} from the {@code OAuth2 principal}.
     * If it doesn't find one, it throws an exception.
     * The potential attributes are: {@code sub}, {@code id}, {@code user_id}, can be extended in the future.
     * @param oAuth2User
     * @return the extracted {@code ID} as a {@code String}
     */
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
