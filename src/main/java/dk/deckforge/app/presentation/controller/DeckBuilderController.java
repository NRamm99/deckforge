package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.command.CreateDeckCommand;
import dk.deckforge.app.application.dto.DeckBuilderCardView;
import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.CardService;
import dk.deckforge.app.application.service.CollectionService;
import dk.deckforge.app.application.service.DeckService;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.presentation.controller.form.DeckBuilderOptions;
import dk.deckforge.app.presentation.controller.form.DeckSaveRequest;
import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.enums.CardColor;
import dk.deckforge.app.domain.enums.CardRarity;
import dk.deckforge.app.domain.enums.CardType;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.enums.Visibility;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@SessionAttributes("deckCards")
public class DeckBuilderController {

    private final CardService cardService;
    private final CollectionService collectionService;
    private final DeckService deckService;
    private final ProfileService profileService;

    public DeckBuilderController(CardService cardService, CollectionService collectionService, DeckService deckService, ProfileService profileService) {
        this.cardService = cardService;
        this.collectionService = collectionService;
        this.deckService = deckService;
        this.profileService = profileService;
    }

    @ModelAttribute("deckCards")
    public Map<Long, Integer> deckCards() {
        return new HashMap<>();
    }

    @GetMapping("/deck-builder")
    public String deckBuilder(@ModelAttribute("filter") Card filter,
                              @ModelAttribute DeckBuilderOptions options,
                              @ModelAttribute("deckCards") Map<Long, Integer> deckCards,
                              Principal principal,
                              Model model) {
        boolean conceptDeck = options.isConceptDeck();
        ProfileView profile = profileService.getProfileByEmail(principal.getName());
        List<CollectionCard> ownedCards = collectionService.getFilteredCardsForUser(profile.getUserId(), new Card());
        Map<Long, Integer> ownedQuantities = ownedCards.stream()
                .collect(Collectors.toMap(collectionCard -> collectionCard.getCard().getId(), CollectionCard::getQuantity));

        List<DeckBuilderCardView> cards = conceptDeck
                ? cardService.filterCards(cardService.getAllCards(), filter).stream()
                .map(card -> toView(card, ownedQuantities.getOrDefault(card.getId(), 0), deckCards, conceptDeck))
                .collect(Collectors.toList())
                : collectionService.getFilteredCardsForUser(profile.getUserId(), filter).stream()
                .map(collectionCard -> toView(collectionCard.getCard(), collectionCard.getQuantity(), deckCards, conceptDeck))
                .collect(Collectors.toList());

        List<DeckBuilderCardView> selectedCards = deckCards.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> toView(cardService.getCard(entry.getKey()), ownedQuantities.getOrDefault(entry.getKey(), 0), deckCards, conceptDeck))
                .collect(Collectors.toList());

        model.addAttribute("cards", cards);
        model.addAttribute("selectedCards", selectedCards);
        model.addAttribute("deckCardCount", deckCards.values().stream().mapToInt(Integer::intValue).sum());
        model.addAttribute("filter", filter == null ? new Card() : filter);
        model.addAttribute("conceptDeck", conceptDeck);
        model.addAttribute("formats", Arrays.asList(DeckFormat.values()));
        model.addAttribute("selectedFormat", DeckFormat.STANDARD);
        model.addAttribute("visibilities", Arrays.asList(Visibility.values()));
        model.addAttribute("selectedVisibility", Visibility.PUBLIC);
        model.addAttribute("rarities", Arrays.asList(CardRarity.values()));
        model.addAttribute("types", Arrays.asList(CardType.values()));
        model.addAttribute("colors", Arrays.asList(CardColor.values()));
        return "deck-builder";
    }

    @PostMapping("/deck-builder/{cardId}/add")
    public String addCardToDeck(@PathVariable long cardId,
                                @ModelAttribute DeckBuilderOptions options,
                                @ModelAttribute("deckCards") Map<Long, Integer> deckCards,
                                Principal principal,
                                @RequestHeader(value = "Referer", required = false) String referer) {
        cardService.getCard(cardId);
        if (options.isConceptDeck() || canAddOwnedCard(cardId, deckCards, principal)) {
            deckCards.merge(cardId, 1, Integer::sum);
        }
        return "redirect:" + safeDeckBuilderRedirect(referer);
    }

    @PostMapping("/deck-builder/{cardId}/remove")
    public String removeCardFromDeck(@PathVariable long cardId,
                                     @ModelAttribute("deckCards") Map<Long, Integer> deckCards,
                                     @RequestHeader(value = "Referer", required = false) String referer) {
        deckCards.computeIfPresent(cardId, (id, quantity) -> quantity <= 1 ? null : quantity - 1);
        return "redirect:" + safeDeckBuilderRedirect(referer);
    }

    @PostMapping("/deck-builder/clear")
    public String clearDeck(@ModelAttribute("deckCards") Map<Long, Integer> deckCards,
                            @RequestHeader(value = "Referer", required = false) String referer,
                            RedirectAttributes redirectAttributes) {
        deckCards.clear();
        redirectAttributes.addFlashAttribute("success", "Decket er ryddet.");
        return "redirect:" + safeDeckBuilderRedirect(referer);
    }

    @PostMapping("/deck-builder/save")
    public String saveDeck(@ModelAttribute DeckSaveRequest request,
                           @ModelAttribute("deckCards") Map<Long, Integer> deckCards,
                           Principal principal,
                           @RequestHeader(value = "Referer", required = false) String referer,
                           RedirectAttributes redirectAttributes) {
        ProfileView profile = profileService.getProfileByEmail(principal.getName());

        try {
            deckService.saveDeck(new CreateDeckCommand(
                    profile.getUserId(),
                    request.deckName(),
                    request.format(),
                    request.isConceptDeck(),
                    request.effectiveVisibility(),
                    deckCards
            ));
            deckCards.clear();
            redirectAttributes.addFlashAttribute("success", "Decket er gemt.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:" + safeDeckBuilderRedirect(referer);
    }

    private DeckBuilderCardView toView(Card card, int ownedQuantity, Map<Long, Integer> deckCards, boolean conceptDeck) {
        return new DeckBuilderCardView(card, ownedQuantity, deckCards.getOrDefault(card.getId(), 0), conceptDeck);
    }

    private boolean canAddOwnedCard(long cardId, Map<Long, Integer> deckCards, Principal principal) {
        ProfileView profile = profileService.getProfileByEmail(principal.getName());
        int ownedQuantity = collectionService.getFilteredCardsForUser(profile.getUserId(), new Card()).stream()
                .filter(collectionCard -> collectionCard.getCard().getId() == cardId)
                .mapToInt(CollectionCard::getQuantity)
                .findFirst()
                .orElse(0);

        return deckCards.getOrDefault(cardId, 0) < ownedQuantity;
    }

    private String safeDeckBuilderRedirect(String referer) {
        if (referer == null || referer.isBlank()) {
            return "/deck-builder";
        }

        int deckBuilderIndex = referer.indexOf("/deck-builder");
        if (deckBuilderIndex < 0) {
            return "/deck-builder";
        }

        return referer.substring(deckBuilderIndex);
    }
}
