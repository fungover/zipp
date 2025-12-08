package org.fungover.zipp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexControllerTEMPNAMETOFIXGITISSUE {

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        model.addAttribute("title", "Zipp");

        boolean loggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isLoggedIn", loggedIn);

        if (loggedIn && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String userName = oAuth2User.getAttribute("name");
            model.addAttribute("userName", userName);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Login");
        return "pages/login";
    }
}
