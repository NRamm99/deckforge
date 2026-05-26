package dk.deckforge.app.domain.model;

import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.enums.Visibility;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeckTest {

    @Test
    void createBuildsValidDeckAndNormalizesNameAndVisibility() {
        Deck deck = Deck.create(
                42L,
                "  My deck  ",
                DeckFormat.STANDARD,
                false,
                null,
                DeckCards.from(Map.of(1L, 10, 2L, 10)),
                Map.of(1L, 10, 2L, 10)
        );

        assertThat(deck.getUserAccountId()).isEqualTo(42L);
        assertThat(deck.getName()).isEqualTo("My deck");
        assertThat(deck.getFormat()).isEqualTo(DeckFormat.STANDARD);
        assertThat(deck.isConceptDeck()).isFalse();
        assertThat(deck.getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(deck.getCardCount()).isEqualTo(20);
    }

    @Test
    void createRejectsBlankName() {
        assertThatThrownBy(() -> Deck.create(
                42L,
                " ",
                DeckFormat.STANDARD,
                false,
                Visibility.PUBLIC,
                DeckCards.from(Map.of(1L, 20)),
                Map.of(1L, 20)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Decknavn er påkrævet.");
    }

    @Test
    void createRejectsMissingFormat() {
        assertThatThrownBy(() -> Deck.create(
                42L,
                "My deck",
                null,
                false,
                Visibility.PUBLIC,
                DeckCards.from(Map.of(1L, 20)),
                Map.of(1L, 20)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vælg et format.");
    }

    @Test
    void createRejectsDeckOutsideFormatSize() {
        assertThatThrownBy(() -> Deck.create(
                42L,
                "Too small",
                DeckFormat.STANDARD,
                false,
                Visibility.PUBLIC,
                DeckCards.from(Map.of(1L, 5)),
                Map.of(1L, 5)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("STANDARD kræver 20-30 kort. Decket har 5.");
    }

    @Test
    void createRejectsCollectionDeckWithMoreCopiesThanOwned() {
        assertThatThrownBy(() -> Deck.create(
                42L,
                "Not owned",
                DeckFormat.STANDARD,
                false,
                Visibility.PUBLIC,
                DeckCards.from(Map.of(1L, 20)),
                Map.of(1L, 3)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Decket indeholder flere kopier af et kort, end du ejer.");
    }

    @Test
    void createAllowsConceptDeckWithoutOwnedCopies() {
        Deck deck = Deck.create(
                42L,
                "Concept",
                DeckFormat.STANDARD,
                true,
                Visibility.PRIVATE,
                DeckCards.from(Map.of(1L, 20)),
                Map.of()
        );

        assertThat(deck.isConceptDeck()).isTrue();
        assertThat(deck.getVisibility()).isEqualTo(Visibility.PRIVATE);
    }
}
