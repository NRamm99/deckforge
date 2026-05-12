package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.service.CardService;
import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.model.CardColor;
import dk.deckforge.app.domain.model.CardRarity;
import dk.deckforge.app.domain.model.CardType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
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
        List<Card> cards = cardService.getAllCards();
        if (filter != null && hasAnyFilter(filter)) {
            cards = cards.stream()
                    .filter(c -> matchesFilter(c, filter))
                    .collect(Collectors.toList());
        }

        model.addAttribute("cards", cards);
        model.addAttribute("filter", filter == null ? new Card() : filter);
        model.addAttribute("rarities", Arrays.asList(CardRarity.values()));
        model.addAttribute("types", Arrays.asList(CardType.values()));
        model.addAttribute("colors", Arrays.asList(CardColor.values()));
        return "card-db";
    }

    private boolean hasAnyFilter(Card filter) {
        return notBlank(filter.getName())
                || notBlank(filter.getCardSet())
                || filter.getRarity() != null
                || filter.getCardType() != null
                || filter.getColor() != null
                || notBlank(filter.getMana());
    }

    private boolean matchesFilter(Card card, Card filter) {
        if (card == null) return false;

        if (notBlank(filter.getName()) && containsIgnoreCase(card.getName(), filter.getName())) return false;
        if (notBlank(filter.getCardSet()) && containsIgnoreCase(card.getCardSet(), filter.getCardSet())) return false;
        if (filter.getRarity() != null && card.getRarity() != filter.getRarity()) return false;
        if (filter.getCardType() != null && card.getCardType() != filter.getCardType()) return false;
        if (filter.getColor() != null && card.getColor() != filter.getColor()) return false;
        return !notBlank(filter.getMana()) || !containsIgnoreCase(card.getMana(), filter.getMana());
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null) return true;
        if (needle == null) return false;
        return !haystack.toLowerCase(Locale.ROOT).contains(needle.trim().toLowerCase(Locale.ROOT));
    }
}
