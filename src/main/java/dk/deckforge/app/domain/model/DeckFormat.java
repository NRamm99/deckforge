package dk.deckforge.app.domain.model;

public enum DeckFormat {
    COMMANDER(10, 10),
    STANDARD(20, 30);

    private final int minDeckSize;
    private final int maxDeckSize;

    DeckFormat(int minDeckSize, int maxDeckSize) {
        this.minDeckSize = minDeckSize;
        this.maxDeckSize = maxDeckSize;
    }

    public int getMinDeckSize() {
        return minDeckSize;
    }

    public int getMaxDeckSize() {
        return maxDeckSize;
    }
}
