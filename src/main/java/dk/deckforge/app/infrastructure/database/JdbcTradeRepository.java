package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Trade;
import dk.deckforge.app.domain.model.TradeStatus;
import dk.deckforge.app.domain.repository.CardRepository;
import dk.deckforge.app.domain.repository.TradeRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcTradeRepository implements TradeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CardRepository cardRepository;

    public JdbcTradeRepository(DataSource dataSource, CardRepository cardRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.cardRepository = cardRepository;
    }

    @Override
    public Trade save(Trade trade) {
        String tradeSql = "INSERT INTO trade (creator_user_id, status) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(tradeSql, new String[]{"id"});
            ps.setLong(1, trade.getCreatorUserId());
            ps.setString(2, trade.getStatus().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Trade id was not generated");
        }
        trade.setId(key.longValue());

        String tradeCardSql = "INSERT INTO trade_card (trade_id, card_id, quantity) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(
                tradeCardSql,
                trade.getOfferedCards(),
                100,
                (ps, cc) -> {
                    ps.setLong(1, trade.getId());
                    ps.setLong(2, cc.getCard().getId());
                    ps.setInt(3, cc.getQuantity());
                }
        );

        return trade;
    }

    @Override
    public Optional<Trade> findById(long id) {
        String tradeSql = "SELECT * FROM trade WHERE id = ?";
        List<Trade> trades = jdbcTemplate.query(tradeSql, (rs, rowNum) -> mapTradeRow(rs), id);
        if (trades.isEmpty()) {
            return Optional.empty();
        }

        Trade trade = trades.get(0);
        trade.setOfferedCards(findTradeCards(trade.getId()));
        return Optional.of(trade);
    }

    @Override
    public List<Trade> findOpenTrades() {
        String tradeSql = "SELECT * FROM trade WHERE status = ? ORDER BY id DESC";
        List<Trade> trades = jdbcTemplate.query(tradeSql, (rs, rowNum) -> mapTradeRow(rs), TradeStatus.OPEN.name());
        for (Trade trade : trades) {
            trade.setOfferedCards(findTradeCards(trade.getId()));
        }
        return trades;
    }

    @Override
    public Trade lockById(long tradeId) {
        String sql = "SELECT * FROM trade WHERE id = ? FOR UPDATE";
        List<Trade> trades = jdbcTemplate.query(sql, (rs, rowNum) -> mapTradeRow(rs), tradeId);
        if (trades.isEmpty()) {
            throw new IllegalArgumentException("Trade not found");
        }
        Trade trade = trades.get(0);
        trade.setOfferedCards(findTradeCards(tradeId));
        return trade;
    }

    @Override
    public void updateStatus(long tradeId, TradeStatus status) {
        String sql = "UPDATE trade SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), tradeId);
    }

    private List<CollectionCard> findTradeCards(long tradeId) {
        String sql = """
                SELECT card_id, quantity
                FROM trade_card
                WHERE trade_id = ?
                ORDER BY card_id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long cardId = rs.getLong("card_id");
            int quantity = rs.getInt("quantity");

            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
            return new CollectionCard(card, quantity);
        }, tradeId);
    }

    private Trade mapTradeRow(ResultSet rs) throws SQLException {
        Trade trade = new Trade();
        trade.setId(rs.getLong("id"));
        trade.setCreatorUserId(rs.getLong("creator_user_id"));
        trade.setStatus(TradeStatus.valueOf(rs.getString("status")));
        return trade;
    }
}

