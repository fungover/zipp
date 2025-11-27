package org.fungover.zipp.Profile;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginSettingsController {

    @GetMapping("/loginsettings")
    public String loginSettings() {
        return "loginsettings";
    }
}
