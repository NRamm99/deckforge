package dk.deckforge.app.presentation.controller.form;

public record DeckBuilderOptions(Boolean conceptDeck) {

    public boolean isConceptDeck() {
        return Boolean.TRUE.equals(conceptDeck);
    }
}
