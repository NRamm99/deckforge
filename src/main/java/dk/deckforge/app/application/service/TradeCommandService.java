package dk.deckforge.app.application.service;

import dk.deckforge.app.application.command.CardQuantitiesCommand;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Trade;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class TradeCommandService {

    private final TradeService tradeService;
    private final CardService cardService;

    public TradeCommandService(TradeService tradeService, CardService cardService) {
        this.tradeService = tradeService;
        this.cardService = cardService;
    }

    public Trade createOpenTrade(long creatorUserId, CardQuantitiesCommand offeredCards) {
        requireNonNull(offeredCards, "offeredCards");
        return tradeService.createOpenTrade(creatorUserId, toCollectionCards(offeredCards));
    }

    public void createOffer(long tradeId, long offerUserId, CardQuantitiesCommand offeredCards) {
        requireNonNull(offeredCards, "offeredCards");
        tradeService.createOffer(tradeId, offerUserId, toCollectionCards(offeredCards));
    }

    private List<CollectionCard> toCollectionCards(CardQuantitiesCommand cmd) {
        return cmd.cardQuantities().entrySet().stream()
                .map(e -> new CollectionCard(
                        cardService.getCard(e.getKey()),
                        e.getValue()
                ))
                .toList();
    }
}

