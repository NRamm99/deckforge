package dk.deckforge.app.application.service;

import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Trade;
import dk.deckforge.app.domain.model.TradeOffer;
import dk.deckforge.app.domain.model.TradeOfferStatus;
import dk.deckforge.app.domain.model.TradeStatus;
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

    public TradeService(TradeRepository tradeRepository,
                        TradeOfferRepository tradeOfferRepository,
                        CollectionRepository collectionRepository) {
        this.tradeRepository = tradeRepository;
        this.tradeOfferRepository = tradeOfferRepository;
        this.collectionRepository = collectionRepository;
    }

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
        return tradeRepository.save(trade);
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

    public TradeOffer createOffer(long tradeId, long offerUserId, List<CollectionCard> offeredCards) {
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
        return tradeOfferRepository.save(offer);
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

        // Lock + validate inventory
        for (CollectionCard cc : trade.getOfferedCards()) {
            collectionRepository.requireSufficientQuantityForUpdate(actingUserId, cc.getCard().getId(), cc.getQuantity());
        }
        for (CollectionCard cc : offer.getOfferedCards()) {
            collectionRepository.requireSufficientQuantityForUpdate(offer.getOfferUserId(), cc.getCard().getId(), cc.getQuantity());
        }

        // Exchange cards
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
    }

    public void cancelTrade(long tradeId, long actingUserId) {
        Trade trade = tradeRepository.lockById(tradeId);
        if (trade.getCreatorUserId() != actingUserId) {
            throw new IllegalStateException("Trade not owned by user");
        }
        if (trade.getStatus() != TradeStatus.OPEN) {
            throw new IllegalStateException("Trade is not open");
        }
        tradeRepository.updateStatus(tradeId, TradeStatus.CANCELLED);
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
