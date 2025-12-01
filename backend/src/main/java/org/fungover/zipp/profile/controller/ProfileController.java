package org.fungover.zipp.profile.controller;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/loginsettings")
    public String showProfile(Model model, Authentication authentication) {

        OAuth2User oauth = (OAuth2User) authentication.getPrincipal();
        String email = oauth.getAttribute("email");

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found in DB"));

        model.addAttribute("user", user);

        return "loginsettings";
    }

    @PostMapping("/loginsettings")
    public String updateProfile(@ModelAttribute("user") User formUser,
                                Authentication authentication) {

        OAuth2User oauth = (OAuth2User) authentication.getPrincipal();
        String email = oauth.getAttribute("email");

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found in DB"));

        user.setDisplayName(formUser.getDisplayName());
        user.setCity(formUser.getCity());
        user.setBio(formUser.getBio());

        userRepository.save(user);
        return "redirect:/loginsettings";
    }
}
