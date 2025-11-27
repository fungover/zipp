package org.fungover.zipp.service;

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

    public CustomOAuth2UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("Custom0ath2UserService loadUser1");
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("Custom0ath2UserService loadUser2");

        String provider_Id = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        //String provider = oAuth2User.getAttribute("provider");

        String provider = userRequest.getClientRegistration().getRegistrationId();

        User user = userRepository.findByProviderAndProviderId(provider, provider_Id)
          .orElseGet(() -> new User());

        //update fields
        user.setProviderId(provider_Id);
        user.setEmail(email);
        user.setName(name);
        user.setProvider(provider);

        userRepository.save(user);


        return oAuth2User;
    }

}
