package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.Trade;
import dk.deckforge.app.domain.enums.TradeStatus;

import java.util.List;
import java.util.Optional;

public interface TradeRepository {
    Trade save(Trade trade);
    Optional<Trade> findById(long id);
    List<Trade> findOpenTrades();

    Trade lockById(long tradeId);

    void updateStatus(long tradeId, TradeStatus status);

    void deleteById(long tradeId);
}
