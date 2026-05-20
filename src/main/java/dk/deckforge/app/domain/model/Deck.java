package dk.deckforge.app.domain.model;

import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.enums.Visibility;

public class Deck {

    private long id;
    private long userAccountId;
    private String name;
    private DeckFormat format;
    private boolean conceptDeck;
    private Visibility visibility = Visibility.PUBLIC;
    private int cardCount;

    public Deck() {
    }

    public Deck(long userAccountId, String name, DeckFormat format, boolean conceptDeck, Visibility visibility) {
        this.userAccountId = userAccountId;
        this.name = name;
        this.format = format;
        this.conceptDeck = conceptDeck;
        this.visibility = visibility;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeckFormat getFormat() {
        return format;
    }

    public void setFormat(DeckFormat format) {
        this.format = format;
    }

    public boolean isConceptDeck() {
        return conceptDeck;
    }

    public void setConceptDeck(boolean conceptDeck) {
        this.conceptDeck = conceptDeck;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }
}
