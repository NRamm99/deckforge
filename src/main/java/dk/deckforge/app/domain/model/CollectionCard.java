package dk.deckforge.app.domain.model;

public class CollectionCard {

    private final Card card;
    private final int quantity;

    public CollectionCard(Card card, int quantity) {
        this.card = card;
        this.quantity = quantity;
    }

    public Card getCard() {
        return card;
    }

    public int getQuantity() {
        return quantity;
    }
}
