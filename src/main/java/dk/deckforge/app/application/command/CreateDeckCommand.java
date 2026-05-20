package dk.deckforge.app.application.command;

import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.enums.Visibility;

import java.util.Map;

public record CreateDeckCommand(
        long userAccountId,
        String name,
        DeckFormat format,
        boolean conceptDeck,
        Visibility visibility,
        Map<Long, Integer> cardQuantities
) {
}
