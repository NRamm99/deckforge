package dk.deckforge.app.application.service;

import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Trade;
import dk.deckforge.app.domain.model.TradeOffer;
import dk.deckforge.app.domain.enums.TradeOfferStatus;
import dk.deckforge.app.domain.enums.TradeStatus;
import dk.deckforge.app.domain.repository.CardReservationRepository;
import dk.deckforge.app.domain.repository.CollectionRepository;
import dk.deckforge.app.domain.repository.TradeOfferRepository;
import dk.deckforge.app.domain.repository.TradeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import static java.util.Objects.requireNonNull;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeOfferRepository tradeOfferRepository;
    private final CollectionRepository collectionRepository;
    private final CardReservationRepository cardReservationRepository;

    public TradeService(TradeRepository tradeRepository,
                        TradeOfferRepository tradeOfferRepository,
                        CollectionRepository collectionRepository,
                        CardReservationRepository cardReservationRepository) {
        this.tradeRepository = tradeRepository;
        this.tradeOfferRepository = tradeOfferRepository;
        this.collectionRepository = collectionRepository;
        this.cardReservationRepository = cardReservationRepository;
    }

    @Transactional
    public Trade createOpenTrade(long creatorUserId, List<CollectionCard> offeredCards) {
        requireNonNull(offeredCards, "offeredCards");
        if (offeredCards.isEmpty()) {
            throw new IllegalArgumentException("Trade must offer at least one card");
        }
        ensureUserHasCards(creatorUserId, offeredCards);

        Trade trade = new Trade();
        trade.setCreatorUserId(creatorUserId);
        trade.setOfferedCards(offeredCards);
        trade.setStatus(TradeStatus.OPEN);
        Trade created = tradeRepository.save(trade);
        cardReservationRepository.reserve("TRADE", created.getId(), creatorUserId, offeredCards);
        return created;
    }

    public List<Trade> listOpenTrades() {
        return tradeRepository.findOpenTrades();
    }

    public Trade getTrade(long tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));
    }

    public List<TradeOffer> listOffersForTrade(long tradeId) {
        return tradeOfferRepository.findByTradeId(tradeId);
    }

    @Transactional
    public void createOffer(long tradeId, long offerUserId, List<CollectionCard> offeredCards) {
        requireNonNull(offeredCards, "offeredCards");
        if (offeredCards.isEmpty()) {
            throw new IllegalArgumentException("Offer must include at least one card");
        }

        Trade trade = getTrade(tradeId);
        if (trade.getStatus() != TradeStatus.OPEN) {
            throw new IllegalStateException("Trade is not open");
        }
        if (trade.getCreatorUserId() == offerUserId) {
            throw new IllegalArgumentException("Cannot offer on your own trade");
        }

        ensureUserHasCards(offerUserId, offeredCards);

        TradeOffer offer = new TradeOffer();
        offer.setTradeId(tradeId);
        offer.setOfferUserId(offerUserId);
        offer.setOfferedCards(offeredCards);
        offer.setStatus(TradeOfferStatus.PENDING);
        tradeOfferRepository.save(offer);
        cardReservationRepository.reserve("OFFER", offer.getId(), offerUserId, offeredCards);
    }

    @Transactional
    public void acceptOffer(long tradeId, long offerId, long actingUserId) {
        Trade trade = tradeRepository.lockById(tradeId);
        if (trade.getStatus() != TradeStatus.OPEN) {
            throw new IllegalStateException("Trade is not open");
        }
        if (trade.getCreatorUserId() != actingUserId) {
            throw new IllegalStateException("Only the trade creator can accept offers");
        }

        TradeOffer offer = tradeOfferRepository.lockById(offerId);
        if (offer.getTradeId() != tradeId) {
            throw new IllegalArgumentException("Offer not found for trade");
        }
        if (offer.getStatus() != TradeOfferStatus.PENDING) {
            throw new IllegalStateException("Offer is not pending");
        }

        // Release the reservations for the accepted trade+offer before moving quantities.
        // This makes the standard "available" checks and decrements work.
        cardReservationRepository.release("TRADE", tradeId);
        cardReservationRepository.release("OFFER", offerId);

        for (CollectionCard cc : trade.getOfferedCards()) {
            collectionRepository.requireSufficientTotalQuantityForUpdate(actingUserId, cc.getCard().getId(), cc.getQuantity());
        }
        for (CollectionCard cc : offer.getOfferedCards()) {
            collectionRepository.requireSufficientTotalQuantityForUpdate(offer.getOfferUserId(), cc.getCard().getId(), cc.getQuantity());
        }

        for (CollectionCard cc : trade.getOfferedCards()) {
            collectionRepository.decrementCardQuantity(actingUserId, cc.getCard().getId(), cc.getQuantity());
            collectionRepository.incrementCardQuantity(offer.getOfferUserId(), cc.getCard().getId(), cc.getQuantity());
        }
        for (CollectionCard cc : offer.getOfferedCards()) {
            collectionRepository.decrementCardQuantity(offer.getOfferUserId(), cc.getCard().getId(), cc.getQuantity());
            collectionRepository.incrementCardQuantity(actingUserId, cc.getCard().getId(), cc.getQuantity());
        }

        tradeRepository.updateStatus(tradeId, TradeStatus.COMPLETED);
        tradeOfferRepository.updateStatus(offerId, TradeOfferStatus.ACCEPTED);
        tradeOfferRepository.declineOtherPendingOffers(tradeId, offerId);

        // Release any remaining reservations tied to this trade (e.g. other pending offers).
        cardReservationRepository.releaseByTradeId(tradeId);
    }

    @Transactional
    public void declineOffer(long tradeId, long offerId, long actingUserId) {
        Trade trade = tradeRepository.lockById(tradeId);
        if (trade.getStatus() != TradeStatus.OPEN) {
            throw new IllegalStateException("Trade is not open");
        }
        if (trade.getCreatorUserId() != actingUserId) {
            throw new IllegalStateException("Only the trade creator can decline offers");
        }

        TradeOffer offer = tradeOfferRepository.lockById(offerId);
        if (offer.getTradeId() != tradeId) {
            throw new IllegalArgumentException("Offer not found for trade");
        }
        if (offer.getStatus() != TradeOfferStatus.PENDING) {
            throw new IllegalStateException("Offer is not pending");
        }

        tradeOfferRepository.updateStatus(offerId, TradeOfferStatus.DECLINED);
        cardReservationRepository.release("OFFER", offerId);
    }

    @Transactional
    public void deleteTrade(long tradeId, long actingUserId) {
        Trade trade = tradeRepository.lockById(tradeId);
        if (trade.getCreatorUserId() != actingUserId) {
            throw new IllegalStateException("Trade not owned by user");
        }
        if (trade.getStatus() == TradeStatus.COMPLETED) {
            throw new IllegalStateException("Completed trades cannot be deleted");
        }

        // "Return" cards by releasing any reservations; ownership was never decremented.
        cardReservationRepository.releaseByTradeId(tradeId);
        tradeRepository.deleteById(tradeId);
    }

    private void ensureUserHasCards(long userAccountId, List<CollectionCard> cards) {
        for (CollectionCard cc : cards) {
            if (cc.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            collectionRepository.requireSufficientQuantityForUpdate(userAccountId, cc.getCard().getId(), cc.getQuantity());
        }
    }
}
