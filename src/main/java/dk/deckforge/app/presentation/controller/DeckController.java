package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.DeckService;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.model.Visibility;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class DeckController {

    private final DeckService deckService;
    private final ProfileService profileService;

    public DeckController(DeckService deckService, ProfileService profileService) {
        this.deckService = deckService;
        this.profileService = profileService;
    }

    @GetMapping("/profile/{id}/decks")
    public String profileDecks(@PathVariable long id, Principal principal, Model model) {
        ProfileView profile = profileService.getProfileByUserId(id);
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());
        boolean ownProfile = currentProfile.getUserId() == profile.getUserId();

        model.addAttribute("profile", profile);
        model.addAttribute("decks", deckService.getDecksForUser(profile.getUserId()));
        model.addAttribute("ownProfile", ownProfile);
        return "decks";
    }

    @GetMapping("/profile/{id}/decks/{deckId}")
    public String viewDeck(@PathVariable long id, @PathVariable long deckId, Principal principal, Model model) {
        ProfileView profile = profileService.getProfileByUserId(id);
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());
        boolean ownProfile = currentProfile.getUserId() == profile.getUserId();

        Deck deck = deckService.getDeckForUser(profile.getUserId(), deckId);
        if (!ownProfile && deck.getVisibility() != Visibility.PUBLIC) {
            model.addAttribute("profile", profile);
            model.addAttribute("deck", deck);
            return "deck-private";
        }

        model.addAttribute("profile", profile);
        model.addAttribute("deck", deck);
        model.addAttribute("cards", deckService.getDeckCards(deckId));
        model.addAttribute("ownProfile", ownProfile);
        return "deck-view";
    }

    @PostMapping("/profile/{id}/decks/{deckId}/delete")
    public String deleteDeck(@PathVariable long id,
                             @PathVariable long deckId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        ProfileView profile = profileService.getProfileByUserId(id);
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        if (currentProfile.getUserId() != profile.getUserId()) {
            return "redirect:/profile/" + id + "/decks";
        }

        deckService.deleteDeck(currentProfile.getUserId(), deckId);
        redirectAttributes.addFlashAttribute("success", "Decket er slettet.");
        return "redirect:/profile/" + id + "/decks";
    }
}
