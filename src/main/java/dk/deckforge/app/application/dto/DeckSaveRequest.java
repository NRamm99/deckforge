package dk.deckforge.app.application.dto;

import dk.deckforge.app.domain.model.DeckFormat;
import dk.deckforge.app.domain.model.Visibility;

public record DeckSaveRequest(String deckName, DeckFormat format, Visibility visibility, Boolean conceptDeck) {

    public boolean isConceptDeck() {
        return Boolean.TRUE.equals(conceptDeck);
    }

    public Visibility effectiveVisibility() {
        return visibility == null ? Visibility.PUBLIC : visibility;
    }
}
