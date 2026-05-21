package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.CollectionCard;

import java.util.List;

public interface CardReservationRepository {
    void reserve(String reservationType, long reservationId, long userAccountId, List<CollectionCard> cards);

    void release(String reservationType, long reservationId);

    void releaseByTradeId(long tradeId);
}

