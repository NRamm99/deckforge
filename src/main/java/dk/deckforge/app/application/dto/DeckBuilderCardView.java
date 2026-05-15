package dk.deckforge.app.application.dto;

import dk.deckforge.app.domain.model.Card;

public class DeckBuilderCardView {

    private final Card card;
    private final int ownedQuantity;
    private final int deckQuantity;
    private final boolean conceptDeck;

    public DeckBuilderCardView(Card card, int ownedQuantity, int deckQuantity, boolean conceptDeck) {
        this.card = card;
        this.ownedQuantity = ownedQuantity;
        this.deckQuantity = deckQuantity;
        this.conceptDeck = conceptDeck;
    }

    public Card getCard() {
        return card;
    }

    public int getOwnedQuantity() {
        return ownedQuantity;
    }

    public int getDeckQuantity() {
        return deckQuantity;
    }

    public boolean isCanAdd() {
        return conceptDeck || deckQuantity < ownedQuantity;
    }
}
