package org.fungover.zipp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
public class indexController {

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        model.addAttribute("title", "Zipp");

        if (authentication != null && authentication.isAuthenticated()){
            if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {

                OAuth2User oauth2user = (OAuth2User) authentication.getPrincipal();

                String userName = oauth2user.getAttribute("name");

                model.addAttribute("userName", userName);
                model.addAttribute("isLoggedIn", true);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Login");
        return "pages/login";
    }
}
