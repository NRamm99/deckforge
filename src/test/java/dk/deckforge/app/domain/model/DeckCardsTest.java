package dk.deckforge.app.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeckCardsTest {

    @Test
    void fromRejectsMissingCards() {
        assertThatThrownBy(() -> DeckCards.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Decket har ingen kort.");

        assertThatThrownBy(() -> DeckCards.from(Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Decket har ingen kort.");
    }

    @Test
    void fromRejectsInvalidQuantities() {
        assertThatThrownBy(() -> DeckCards.from(Map.of(1L, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kortantal skal være større end 0.");
    }

    @Test
    void totalQuantitySumsAllCards() {
        DeckCards cards = DeckCards.from(Map.of(1L, 7, 2L, 13));

        assertThat(cards.totalQuantity()).isEqualTo(20);
        assertThat(cards.asMap()).containsEntry(1L, 7).containsEntry(2L, 13);
    }
}
