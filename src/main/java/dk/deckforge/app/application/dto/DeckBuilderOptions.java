package dk.deckforge.app.application.dto;

public record DeckBuilderOptions(Boolean conceptDeck) {

    public boolean isConceptDeck() {
        return Boolean.TRUE.equals(conceptDeck);
    }
}
