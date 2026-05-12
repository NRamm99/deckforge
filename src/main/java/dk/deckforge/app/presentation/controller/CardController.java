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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/card-db")
    public String cardDatabase(Model model) {
        model.addAttribute("cards", cardService.getAllCards());
        model.addAttribute("cardForm", new Card());
        model.addAttribute("rarities", Arrays.asList(CardRarity.values()));
        model.addAttribute("types", Arrays.asList(CardType.values()));
        model.addAttribute("colors", Arrays.asList(CardColor.values()));
        return "card-db";
    }

    @PostMapping("/card-db")
    public String createCard(@ModelAttribute("cardForm") Card card, RedirectAttributes redirectAttributes) {
        try {
            cardService.createCard(card);
            redirectAttributes.addFlashAttribute("successMessage", "Card created");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/card-db";
    }
}
