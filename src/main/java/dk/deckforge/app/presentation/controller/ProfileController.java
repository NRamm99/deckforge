package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.command.UpdateDebugProfileCommand;
import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.domain.model.Role;
import dk.deckforge.app.domain.model.Visibility;
import dk.deckforge.app.presentation.controller.form.ProfileDebugRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final UserDetailsService userDetailsService;

    public ProfileController(ProfileService profileService, UserDetailsService userDetailsService) {
        this.profileService = profileService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/profile")
    public String ownProfile(Principal principal, Model model) {
        ProfileView profile = profileService.getProfileByEmail(principal.getName());
        model.addAttribute("profile", profile);
        model.addAttribute("avatarOptions", avatarOptions());
        return "profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(String avatarUrl, Principal principal, RedirectAttributes redirectAttributes) {
        profileService.updateAvatarUrl(principal.getName(), avatarUrl);
        redirectAttributes.addFlashAttribute("success", "Profilbillede er opdateret.");
        return "redirect:/profile";
    }

    @GetMapping("/profile/debug")
    public String debugProfile(Principal principal, Model model) {
        ProfileView profile = profileService.getProfileByEmail(principal.getName());
        addDebugAttributes(model, profile);
        return "profile-debug";
    }

    @PostMapping("/profile/debug")
    public String updateDebugProfile(@ModelAttribute ProfileDebugRequest request,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            ProfileView updatedProfile = profileService.updateDebugProfile(new UpdateDebugProfileCommand(
                    principal.getName(),
                    request.email(),
                    request.displayName(),
                    request.password(),
                    request.role(),
                    request.collectionVisibility()
            ));
            refreshAuthentication(updatedProfile.getEmail());
            redirectAttributes.addFlashAttribute("success", "Debug-profilen er gemt.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/profile/debug";
    }

    @GetMapping("/profile/{id}")
    public String otherProfile(@PathVariable Long id, Model model) {
        ProfileView profile = profileService.getProfileByUserId(id);
        model.addAttribute("profile", profile);
        return "profile";
    }

    private void addDebugAttributes(Model model, ProfileView profile) {
        model.addAttribute("profile", profile);
        model.addAttribute("roles", Arrays.asList(Role.values()));
        model.addAttribute("visibilities", Arrays.asList(Visibility.values()));
    }

    private void refreshAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private List<String> avatarOptions() {
        return List.of(
                "/images/avatars/Jace.jpg",
                "/images/avatars/Liliana.jpg",
                "/images/avatars/Chandra.jpg",
                "/images/avatars/Nissa.jpg",
                "/images/avatars/Teferi.jpg",
                "/images/avatars/Garruk.jpg",
                "/images/avatars/Ajani.jpg",
                "/images/avatars/Nicol.jpg",
                "/images/avatars/Karn.jpg",
                "/images/avatars/Sorin.jpg"
        );
    }
}
