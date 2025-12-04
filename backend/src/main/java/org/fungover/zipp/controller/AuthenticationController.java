package org.fungover.zipp.controller;

import org.fungover.zipp.repository.UserRepository;
import org.fungover.zipp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    private final ClientRegistrationRepository repo;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthenticationController(ClientRegistrationRepository repo, UserService userService,
            UserRepository userRepository) {
        this.repo = repo;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Example of accessing values from OAuth2 via Controller can be used to test
     * user login
     * 
     * @param principal
     *            returns String Name and email of logged-in user
     * @return
     */
    @GetMapping("/me")
    public String greet(@AuthenticationPrincipal OAuth2User principal) {
        String name = principal.getAttribute("name");
        String email = principal.getAttribute("email");
        String id = principal.getAttribute("sub");

        return "Hello " + name + ", your email adress is: " + email + ", this is your id " + id;
    }

}
