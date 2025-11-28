package org.fungover.zipp.controller;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final UserRepository userRepository;

    public ViewController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/loginsettings")
    public String loginSettings(Model model, Authentication authentication) {

        OAuth2User oauth = (OAuth2User) authentication.getPrincipal();
        String email = oauth.getAttribute("email");

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found in DB"));

        model.addAttribute("user", user);

        return "loginsettings";
    }
}
