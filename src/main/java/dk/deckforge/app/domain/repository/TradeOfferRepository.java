package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.TradeOffer;
import dk.deckforge.app.domain.enums.TradeOfferStatus;

import java.util.List;
import java.util.Optional;

public interface TradeOfferRepository {
    TradeOffer save(TradeOffer offer);
    Optional<TradeOffer> findById(long id);
    List<TradeOffer> findByTradeId(long tradeId);

    TradeOffer lockById(long offerId);

    void updateStatus(long offerId, TradeOfferStatus status);

    void declineOtherPendingOffers(long tradeId, long acceptedOfferId);
}
