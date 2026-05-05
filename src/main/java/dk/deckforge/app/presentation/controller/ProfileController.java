package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String ownProfile(Principal principal, Model model) {
        ProfileView profile = profileService.getProfileByEmail(principal.getName());
        model.addAttribute("profile", profile);
        return "profile";
    }

    @GetMapping("/profile/{id}")
    public String otherProfile(@PathVariable Long id, Model model) {
        ProfileView profile = profileService.getProfileByUserId(id);
        model.addAttribute("profile", profile);
        return "profile";
    }
}
