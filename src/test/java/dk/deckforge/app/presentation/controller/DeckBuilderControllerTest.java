package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.dto.DeckBuilderCardView;
import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.CardService;
import dk.deckforge.app.application.service.CollectionService;
import dk.deckforge.app.application.service.DeckService;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.domain.enums.Role;
import dk.deckforge.app.domain.enums.Visibility;
import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.presentation.controller.form.DeckBuilderOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckBuilderControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private CollectionService collectionService;

    @Mock
    private DeckService deckService;

    @Mock
    private ProfileService profileService;

    private DeckBuilderController controller;

    @BeforeEach
    void setUp() {
        controller = new DeckBuilderController(cardService, collectionService, deckService, profileService);
    }

    @Test
    void deckBuilderAddsSelectedCardsAndDeckCountToModel() {
        Card lightningBolt = card(1L, "Lightning Bolt");
        Card island = card(2L, "Island");
        Map<Long, Integer> deckCards = new HashMap<>();
        deckCards.put(lightningBolt.getId(), 2);

        when(profileService.getProfileByEmail("player@example.com")).thenReturn(profile());
        when(collectionService.getFilteredCardsForUser(eq(42L), any(Card.class)))
                .thenReturn(List.of(new CollectionCard(lightningBolt, 4), new CollectionCard(island, 3)));
        when(cardService.getCard(lightningBolt.getId())).thenReturn(lightningBolt);

        Model model = new ExtendedModelMap();
        String view = controller.deckBuilder(new Card(), new DeckBuilderOptions(false), deckCards, principal(), model);

        assertThat(view).isEqualTo("deck-builder");
        assertThat(model.getAttribute("deckCardCount")).isEqualTo(2);

        @SuppressWarnings("unchecked")
        List<DeckBuilderCardView> selectedCards = (List<DeckBuilderCardView>) model.getAttribute("selectedCards");
        assertThat(selectedCards).hasSize(1);
        assertThat(selectedCards.get(0).getCard().getName()).isEqualTo("Lightning Bolt");
        assertThat(selectedCards.get(0).getDeckQuantity()).isEqualTo(2);
        assertThat(selectedCards.get(0).getOwnedQuantity()).isEqualTo(4);
    }

    @Test
    void addCardToDeckDoesNotAddMoreCopiesThanOwnedForCollectionDeck() {
        Card lightningBolt = card(1L, "Lightning Bolt");
        Map<Long, Integer> deckCards = new HashMap<>();
        deckCards.put(lightningBolt.getId(), 1);

        when(cardService.getCard(lightningBolt.getId())).thenReturn(lightningBolt);
        when(profileService.getProfileByEmail("player@example.com")).thenReturn(profile());
        when(collectionService.getFilteredCardsForUser(eq(42L), any(Card.class)))
                .thenReturn(List.of(new CollectionCard(lightningBolt, 1)));

        String redirect = controller.addCardToDeck(
                lightningBolt.getId(),
                new DeckBuilderOptions(false),
                deckCards,
                principal(),
                "http://localhost:8080/deck-builder"
        );

        assertThat(redirect).isEqualTo("redirect:/deck-builder");
        assertThat(deckCards).containsEntry(lightningBolt.getId(), 1);
    }

    @Test
    void addCardToDeckAllowsExtraCopiesForConceptDeck() {
        Card lightningBolt = card(1L, "Lightning Bolt");
        Map<Long, Integer> deckCards = new HashMap<>();
        deckCards.put(lightningBolt.getId(), 1);

        when(cardService.getCard(lightningBolt.getId())).thenReturn(lightningBolt);

        String redirect = controller.addCardToDeck(
                lightningBolt.getId(),
                new DeckBuilderOptions(true),
                deckCards,
                principal(),
                "http://localhost:8080/deck-builder?conceptDeck=true"
        );

        assertThat(redirect).isEqualTo("redirect:/deck-builder?conceptDeck=true");
        assertThat(deckCards).containsEntry(lightningBolt.getId(), 2);
        verify(cardService).getCard(lightningBolt.getId());
    }

    private static Principal principal() {
        return () -> "player@example.com";
    }

    private static ProfileView profile() {
        return new ProfileView(42L, "player@example.com", "Player", Visibility.PUBLIC, Role.USER, null);
    }

    private static Card card(long id, String name) {
        Card card = new Card();
        card.setId(id);
        card.setName(name);
        return card;
    }
}
