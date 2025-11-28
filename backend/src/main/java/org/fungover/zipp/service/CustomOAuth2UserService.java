package org.fungover.zipp.service;

import org.fungover.zipp.entity.Role;
import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(
      OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String providerId = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        String provider = userRequest
          .getClientRegistration()
          .getRegistrationId();

        User user = userRepository
          .findByProviderAndProviderId(provider, providerId)
          .orElseGet(() -> {
              User newUser = new User();

              newUser.setProviderId(providerId);
              newUser.setEmail(email);
              newUser.setName(name);
              newUser.setProvider(provider);
              newUser.setRole(Role.USER);

              return newUser;
          });

        userRepository.save(user);

        return oAuth2User;
    }

}
