package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.CardService;
import dk.deckforge.app.application.service.CollectionService;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.enums.CardColor;
import dk.deckforge.app.domain.enums.CardRarity;
import dk.deckforge.app.domain.enums.CardType;
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
import java.util.List;

@Controller
public class CardController {

    private final CardService cardService;
    private final CollectionService collectionService;
    private final ProfileService profileService;

    public CardController(CardService cardService, CollectionService collectionService, ProfileService profileService) {
        this.cardService = cardService;
        this.collectionService = collectionService;
        this.profileService = profileService;
    }

    @GetMapping("/create-card")
    public String createCardForm(Model model) {
        model.addAttribute("card", new Card());
        model.addAttribute("rarities", Arrays.asList(CardRarity.values()));
        model.addAttribute("types", Arrays.asList(CardType.values()));
        model.addAttribute("colors", Arrays.asList(CardColor.values()));
        return "create-card";
    }

    @PostMapping("/create-card")
    public String createCardSubmit(@ModelAttribute("card") Card card, Model model) {
        try {
            cardService.createCard(card);
            return "redirect:/card-db";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("rarities", Arrays.asList(CardRarity.values()));
            model.addAttribute("types", Arrays.asList(CardType.values()));
            model.addAttribute("colors", Arrays.asList(CardColor.values()));
            return "create-card";
        }
    }

    @GetMapping("/card-db")
    public String cardDatabase(@ModelAttribute("filter") Card filter, Model model) {
        List<Card> cards = cardService.filterCards(cardService.getAllCards(), filter);

        model.addAttribute("cards", cards);
        model.addAttribute("filter", filter == null ? new Card() : filter);
        model.addAttribute("rarities", Arrays.asList(CardRarity.values()));
        model.addAttribute("types", Arrays.asList(CardType.values()));
        model.addAttribute("colors", Arrays.asList(CardColor.values()));
        return "card-db";
    }

    @PostMapping("/card-db/{cardId}/add")
    public String addCardToCollection(@PathVariable long cardId,
                                      Principal principal,
                                      @RequestHeader(value = "Referer", required = false) String referer,
                                      RedirectAttributes redirectAttributes) {
        ProfileView profile = profileService.getProfileByEmail(principal.getName());
        collectionService.addCardToUserCollection(profile.getUserId(), cardId);
        redirectAttributes.addFlashAttribute("success", "Kortet er tilføjet til din samling.");
        return "redirect:" + safeCardDbRedirect(referer);
    }

    private String safeCardDbRedirect(String referer) {
        if (referer == null || referer.isBlank()) {
            return "/card-db";
        }

        int cardDbIndex = referer.indexOf("/card-db");
        if (cardDbIndex < 0) {
            return "/card-db";
        }

        return referer.substring(cardDbIndex);
    }
}
