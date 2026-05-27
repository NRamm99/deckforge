package dk.deckforge.app.application.service;

import dk.deckforge.app.domain.enums.TradeOfferStatus;
import dk.deckforge.app.domain.enums.TradeStatus;
import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Trade;
import dk.deckforge.app.domain.model.TradeOffer;
import dk.deckforge.app.domain.repository.CardReservationRepository;
import dk.deckforge.app.domain.repository.CollectionRepository;
import dk.deckforge.app.domain.repository.TradeOfferRepository;
import dk.deckforge.app.domain.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeOfferRepository tradeOfferRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CardReservationRepository cardReservationRepository;

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        tradeService = new TradeService(tradeRepository, tradeOfferRepository, collectionRepository, cardReservationRepository);
    }

    @Test
    void acceptOfferTransfersCardsAndCompletesTrade() {
        long tradeId = 100L;
        long offerId = 200L;
        long creatorUserId = 1L;
        long offerUserId = 2L;

        CollectionCard creatorOffers = new CollectionCard(card(10L), 2);
        CollectionCard offererOffers = new CollectionCard(card(11L), 3);

        Trade trade = new Trade(tradeId, creatorUserId, List.of(creatorOffers), TradeStatus.OPEN);
        TradeOffer offer = new TradeOffer(offerId, tradeId, offerUserId, List.of(offererOffers), TradeOfferStatus.PENDING);

        when(tradeRepository.lockById(tradeId)).thenReturn(trade);
        when(tradeOfferRepository.lockById(offerId)).thenReturn(offer);

        tradeService.acceptOffer(tradeId, offerId, creatorUserId);

        verify(cardReservationRepository).release("TRADE", tradeId);
        verify(cardReservationRepository).release("OFFER", offerId);

        verify(collectionRepository).requireSufficientTotalQuantityForUpdate(creatorUserId, 10L, 2);
        verify(collectionRepository).requireSufficientTotalQuantityForUpdate(offerUserId, 11L, 3);

        verify(collectionRepository).decrementCardQuantity(creatorUserId, 10L, 2);
        verify(collectionRepository).incrementCardQuantity(offerUserId, 10L, 2);

        verify(collectionRepository).decrementCardQuantity(offerUserId, 11L, 3);
        verify(collectionRepository).incrementCardQuantity(creatorUserId, 11L, 3);

        verify(tradeRepository).updateStatus(tradeId, TradeStatus.COMPLETED);
        verify(tradeOfferRepository).updateStatus(offerId, TradeOfferStatus.ACCEPTED);
        verify(tradeOfferRepository).declineOtherPendingOffers(tradeId, offerId);
        verify(cardReservationRepository).releaseByTradeId(tradeId);
    }

    @Test
    void acceptOfferRejectsNonCreator() {
        long tradeId = 100L;
        long offerId = 200L;
        long creatorUserId = 1L;
        long attackerUserId = 999L;

        Trade trade = new Trade(tradeId, creatorUserId, List.of(new CollectionCard(card(10L), 1)), TradeStatus.OPEN);
        when(tradeRepository.lockById(tradeId)).thenReturn(trade);

        assertThatThrownBy(() -> tradeService.acceptOffer(tradeId, offerId, attackerUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only the trade creator can accept offers");

        verify(tradeOfferRepository, never()).lockById(offerId);
        verify(cardReservationRepository, never()).release("TRADE", tradeId);
        verify(tradeRepository, never()).updateStatus(tradeId, TradeStatus.COMPLETED);
        verify(tradeOfferRepository, never()).updateStatus(offerId, TradeOfferStatus.ACCEPTED);
    }

    private static Card card(long id) {
        Card card = new Card();
        card.setId(id);
        card.setName("Card " + id);
        return card;
    }
}

