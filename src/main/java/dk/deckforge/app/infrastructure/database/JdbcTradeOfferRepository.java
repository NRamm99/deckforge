package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.TradeOffer;
import dk.deckforge.app.domain.enums.TradeOfferStatus;
import dk.deckforge.app.domain.repository.CardRepository;
import dk.deckforge.app.domain.repository.TradeOfferRepository;
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
public class JdbcTradeOfferRepository implements TradeOfferRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CardRepository cardRepository;

    public JdbcTradeOfferRepository(DataSource dataSource, CardRepository cardRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.cardRepository = cardRepository;
    }

    @Override
    public TradeOffer save(TradeOffer offer) {
        String offerSql = "INSERT INTO trade_offer (trade_id, offer_user_id, status) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(offerSql, new String[]{"id"});
            ps.setLong(1, offer.getTradeId());
            ps.setLong(2, offer.getOfferUserId());
            ps.setString(3, offer.getStatus().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Trade offer id was not generated");
        }
        offer.setId(key.longValue());

        String offerCardSql = "INSERT INTO trade_offer_card (trade_offer_id, card_id, quantity) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(
                offerCardSql,
                offer.getOfferedCards(),
                100,
                (ps, cc) -> {
                    ps.setLong(1, offer.getId());
                    ps.setLong(2, cc.getCard().getId());
                    ps.setInt(3, cc.getQuantity());
                }
        );

        return offer;
    }

    @Override
    public Optional<TradeOffer> findById(long id) {
        String sql = "SELECT * FROM trade_offer WHERE id = ?";
        List<TradeOffer> offers = jdbcTemplate.query(sql, (rs, rowNum) -> mapOfferRow(rs), id);
        if (offers.isEmpty()) {
            return Optional.empty();
        }
        TradeOffer offer = offers.get(0);
        offer.setOfferedCards(findOfferCards(offer.getId()));
        return Optional.of(offer);
    }

    @Override
    public List<TradeOffer> findByTradeId(long tradeId) {
        String sql = "SELECT * FROM trade_offer WHERE trade_id = ? ORDER BY id DESC";
        List<TradeOffer> offers = jdbcTemplate.query(sql, (rs, rowNum) -> mapOfferRow(rs), tradeId);
        for (TradeOffer offer : offers) {
            offer.setOfferedCards(findOfferCards(offer.getId()));
        }
        return offers;
    }

    @Override
    public TradeOffer lockById(long offerId) {
        String sql = "SELECT * FROM trade_offer WHERE id = ? FOR UPDATE";
        List<TradeOffer> offers = jdbcTemplate.query(sql, (rs, rowNum) -> mapOfferRow(rs), offerId);
        if (offers.isEmpty()) {
            throw new IllegalArgumentException("Offer not found");
        }
        TradeOffer offer = offers.get(0);
        offer.setOfferedCards(findOfferCards(offerId));
        return offer;
    }

    @Override
    public void updateStatus(long offerId, TradeOfferStatus status) {
        String sql = "UPDATE trade_offer SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), offerId);
    }

    @Override
    public void declineOtherPendingOffers(long tradeId, long acceptedOfferId) {
        String sql = """
                UPDATE trade_offer
                SET status = ?
                WHERE trade_id = ? AND id <> ? AND status = ?
                """;
        jdbcTemplate.update(sql, TradeOfferStatus.DECLINED.name(), tradeId, acceptedOfferId, TradeOfferStatus.PENDING.name());
    }

    private List<CollectionCard> findOfferCards(long offerId) {
        String sql = """
                SELECT card_id, quantity
                FROM trade_offer_card
                WHERE trade_offer_id = ?
                ORDER BY card_id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long cardId = rs.getLong("card_id");
            int quantity = rs.getInt("quantity");

            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
            return new CollectionCard(card, quantity);
        }, offerId);
    }

    private TradeOffer mapOfferRow(ResultSet rs) throws SQLException {
        TradeOffer offer = new TradeOffer();
        offer.setId(rs.getLong("id"));
        offer.setTradeId(rs.getLong("trade_id"));
        offer.setOfferUserId(rs.getLong("offer_user_id"));
        offer.setStatus(TradeOfferStatus.valueOf(rs.getString("status")));
        return offer;
    }
}
