package dk.deckforge.app.application.service;

import dk.deckforge.app.application.command.CreateDeckCommand;
import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.enums.Visibility;
import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.repository.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CollectionService collectionService;

    private DeckService deckService;

    @BeforeEach
    void setUp() {
        deckService = new DeckService(deckRepository, collectionService);
    }

    @Test
    void saveDeckPersistsValidCollectionDeck() {
        Map<Long, Integer> cardQuantities = Map.of(1L, 10, 2L, 10);
        when(collectionService.getFilteredCardsForUser(42L, null))
                .thenReturn(List.of(
                        new CollectionCard(card(1L), 10),
                        new CollectionCard(card(2L), 10)
                ));
        when(deckRepository.save(any(Deck.class), eq(cardQuantities)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Deck saved = deckService.saveDeck(new CreateDeckCommand(
                42L,
                "  Standard test deck  ",
                DeckFormat.STANDARD,
                false,
                Visibility.PRIVATE,
                cardQuantities
        ));

        assertThat(saved.getUserAccountId()).isEqualTo(42L);
        assertThat(saved.getName()).isEqualTo("Standard test deck");
        assertThat(saved.getFormat()).isEqualTo(DeckFormat.STANDARD);
        assertThat(saved.isConceptDeck()).isFalse();
        assertThat(saved.getVisibility()).isEqualTo(Visibility.PRIVATE);

        ArgumentCaptor<Deck> deckCaptor = ArgumentCaptor.forClass(Deck.class);
        verify(deckRepository).save(deckCaptor.capture(), eq(cardQuantities));
        assertThat(deckCaptor.getValue().getName()).isEqualTo("Standard test deck");
    }

    @Test
    void saveDeckRejectsDeckBelowFormatMinimum() {
        Map<Long, Integer> cardQuantities = Map.of(1L, 5);

        assertThatThrownBy(() -> deckService.saveDeck(new CreateDeckCommand(
                42L,
                "Too small",
                DeckFormat.STANDARD,
                false,
                Visibility.PUBLIC,
                cardQuantities
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("STANDARD")
                .hasMessageContaining("Decket har 5");

        verify(deckRepository, never()).save(any(), any());
    }

    @Test
    void saveDeckRejectsCollectionDeckWithMoreCopiesThanOwned() {
        Map<Long, Integer> cardQuantities = Map.of(1L, 20);
        when(collectionService.getFilteredCardsForUser(42L, null))
                .thenReturn(List.of(new CollectionCard(card(1L), 3)));

        assertThatThrownBy(() -> deckService.saveDeck(new CreateDeckCommand(
                42L,
                "Not owned",
                DeckFormat.STANDARD,
                false,
                Visibility.PUBLIC,
                cardQuantities
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Decket indeholder flere kopier af et kort, end du ejer.");

        verify(deckRepository, never()).save(any(), any());
    }

    @Test
    void saveDeckAllowsConceptDeckWithoutOwnedCopies() {
        Map<Long, Integer> cardQuantities = Map.of(1L, 20);
        when(deckRepository.save(any(Deck.class), eq(cardQuantities)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Deck saved = deckService.saveDeck(new CreateDeckCommand(
                42L,
                "Concept deck",
                DeckFormat.STANDARD,
                true,
                null,
                cardQuantities
        ));

        assertThat(saved.isConceptDeck()).isTrue();
        assertThat(saved.getVisibility()).isEqualTo(Visibility.PUBLIC);
        verify(collectionService, never()).getFilteredCardsForUser(any(Long.class), any());
        verify(deckRepository).save(any(Deck.class), eq(cardQuantities));
    }

    private static Card card(long id) {
        Card card = new Card();
        card.setId(id);
        card.setName("Card " + id);
        return card;
    }
}
