package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.repository.CardReservationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class JdbcCardReservationRepository implements CardReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCardReservationRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void reserve(String reservationType, long reservationId, long userAccountId, List<CollectionCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO card_reservation (reservation_type, reservation_id, user_account_id, card_id, quantity)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.batchUpdate(
                sql,
                cards,
                100,
                (ps, cc) -> {
                    ps.setString(1, reservationType);
                    ps.setLong(2, reservationId);
                    ps.setLong(3, userAccountId);
                    ps.setLong(4, cc.getCard().getId());
                    ps.setInt(5, cc.getQuantity());
                }
        );
    }

    @Override
    public void release(String reservationType, long reservationId) {
        String sql = "DELETE FROM card_reservation WHERE reservation_type = ? AND reservation_id = ?";
        jdbcTemplate.update(sql, reservationType, reservationId);
    }

    @Override
    public void releaseByTradeId(long tradeId) {
        // Release the trade creator's reservation, plus all offer reservations for that trade.
        // This relies on the offer reservation_id being the trade_offer.id.
        jdbcTemplate.update("DELETE FROM card_reservation WHERE reservation_type = 'TRADE' AND reservation_id = ?", tradeId);
        jdbcTemplate.update("""
                DELETE FROM card_reservation
                WHERE reservation_type = 'OFFER'
                  AND reservation_id IN (SELECT id FROM trade_offer WHERE trade_id = ?)
                """, tradeId);
    }
}
