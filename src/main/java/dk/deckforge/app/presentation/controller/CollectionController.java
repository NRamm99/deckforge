package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.CollectionService;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.enums.CardColor;
import dk.deckforge.app.domain.enums.CardRarity;
import dk.deckforge.app.domain.enums.CardType;
import dk.deckforge.app.domain.enums.Visibility;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;

@Controller
public class CollectionController {

    private final CollectionService collectionService;
    private final ProfileService profileService;

    public CollectionController(CollectionService collectionService, ProfileService profileService) {
        this.collectionService = collectionService;
        this.profileService = profileService;
    }

    @GetMapping("/collection")
    public String ownCollection(@ModelAttribute("filter") Card filter, Principal principal, Model model) {
        ProfileView profile = profileService.getProfileByEmail(principal.getName());
        addCollectionAttributes(model, profile, filter, "/collection", true);
        return "collection";
    }

    @GetMapping("/profile/{id}/collection")
    public String profileCollection(@PathVariable Long id, @ModelAttribute("filter") Card filter, Principal principal, Model model) {
        ProfileView profile = profileService.getProfileByUserId(id);
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());
        boolean ownProfile = currentProfile.getUserId() == profile.getUserId();
        if (!ownProfile && profile.getCollectionVisibility() != Visibility.PUBLIC) {
            model.addAttribute("profile", profile);
            return "collection-private";
        }
        addCollectionAttributes(model, profile, filter, "/profile/" + id + "/collection", ownProfile);
        return "collection";
    }

    @PostMapping("/collection/{cardId}/remove")
    public String removeCardFromCollection(@PathVariable long cardId,
                                           Principal principal,
                                           @RequestHeader(value = "Referer", required = false) String referer,
                                           RedirectAttributes redirectAttributes) {
        ProfileView profile = profileService.getProfileByEmail(principal.getName());
        collectionService.removeCardFromUserCollection(profile.getUserId(), cardId);
        redirectAttributes.addFlashAttribute("success", "Kortet er fjernet fra din samling.");
        return "redirect:" + safeCollectionRedirect(referer);
    }

    private void addCollectionAttributes(Model model, ProfileView profile, Card filter, String collectionAction, boolean canEditCollection) {
        model.addAttribute("profile", profile);
        model.addAttribute("cards", collectionService.getFilteredCardsForUser(profile.getUserId(), filter));
        model.addAttribute("filter", filter == null ? new Card() : filter);
        model.addAttribute("collectionAction", collectionAction);
        model.addAttribute("canEditCollection", canEditCollection);
        model.addAttribute("rarities", Arrays.asList(CardRarity.values()));
        model.addAttribute("types", Arrays.asList(CardType.values()));
        model.addAttribute("colors", Arrays.asList(CardColor.values()));
    }

    private String safeCollectionRedirect(String referer) {
        if (referer == null || referer.isBlank()) {
            return "/collection";
        }

        int collectionIndex = referer.indexOf("/collection");
        int profileIndex = referer.indexOf("/profile/");
        if (profileIndex >= 0 && referer.contains("/collection")) {
            return referer.substring(profileIndex);
        }
        if (collectionIndex >= 0) {
            return referer.substring(collectionIndex);
        }

        return "/collection";
    }
}
