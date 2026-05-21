package dk.deckforge.app.domain.model;

import dk.deckforge.app.domain.enums.TradeOfferStatus;

import java.util.List;

public class TradeOffer {
    private long id;
    private long tradeId;
    private long offerUserId;
    private List<CollectionCard> offeredCards;
    private TradeOfferStatus status;

    public TradeOffer() {
    }

    public TradeOffer(long id, long tradeId, long offerUserId, List<CollectionCard> offeredCards,  TradeOfferStatus status) {
        this.id = id;
        this.tradeId = tradeId;
        this.offerUserId = offerUserId;
        this.offeredCards = offeredCards;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public long getOfferUserId() {
        return offerUserId;
    }

    public void setOfferUserId(long offerUserId) {
        this.offerUserId = offerUserId;
    }

    public List<CollectionCard> getOfferedCards() {
        return offeredCards;
    }

    public void setOfferedCards(List<CollectionCard> offeredCards) {
        this.offeredCards = offeredCards;
    }

    public TradeOfferStatus getStatus() {
        return status;
    }

    public void setStatus(TradeOfferStatus status) {
        this.status = status;
    }


}
