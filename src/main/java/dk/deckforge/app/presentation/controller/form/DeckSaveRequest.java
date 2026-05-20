package dk.deckforge.app.presentation.controller.form;

import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.enums.Visibility;

public record DeckSaveRequest(String deckName, DeckFormat format, Visibility visibility, Boolean conceptDeck) {

    public boolean isConceptDeck() {
        return Boolean.TRUE.equals(conceptDeck);
    }

    public Visibility effectiveVisibility() {
        return visibility == null ? Visibility.PUBLIC : visibility;
    }
}
