package dk.deckforge.app.domain.model;

import dk.deckforge.app.domain.enums.TradeStatus;

import java.util.List;

public class Trade {
    private long id;
    private long creatorUserId;
    private List<CollectionCard> offeredCards;
    private TradeStatus status;

    public Trade() {
    }

    public Trade(long id, long creatorUserId, List<CollectionCard> offeredCards, TradeStatus status) {
        this.id = id;
        this.creatorUserId = creatorUserId;
        this.offeredCards = offeredCards;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public List<CollectionCard> getOfferedCards() {
        return offeredCards;
    }

    public void setOfferedCards(List<CollectionCard> offeredCards) {
        this.offeredCards = offeredCards;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }
}
