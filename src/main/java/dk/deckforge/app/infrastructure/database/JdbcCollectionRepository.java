package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.enums.CardColor;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.enums.CardRarity;
import dk.deckforge.app.domain.enums.CardType;
import dk.deckforge.app.domain.repository.CollectionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcCollectionRepository implements CollectionRepository {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public JdbcCollectionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<CollectionCard> findCardsByUserAccountId(long userAccountId) {
        String sql = """
                SELECT c.*, pcc.quantity
                FROM player_collection_card pcc
                JOIN card c ON c.id = pcc.card_id
                WHERE pcc.user_account_id = ?
                ORDER BY c.id
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userAccountId);

            ResultSet rs = ps.executeQuery();
            List<CollectionCard> cards = new ArrayList<>();
            while (rs.next()) {
                cards.add(new CollectionCard(mapRow(rs), rs.getInt("quantity")));
            }
            return cards;

        } catch (SQLException e) {
            throw new RuntimeException("Error listing player collection cards", e);
        }
    }

    @Override
    public void addCardToUserCollection(long userAccountId, long cardId) {
        String sql = """
                INSERT INTO player_collection_card (user_account_id, card_id, quantity)
                VALUES (?, ?, 1)
                ON DUPLICATE KEY UPDATE quantity = quantity + 1
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userAccountId);
            ps.setLong(2, cardId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error adding card to player collection", e);
        }
    }

    @Override
    public void removeCardFromUserCollection(long userAccountId, long cardId) {
        // Ensure we don't remove a reserved card (availability check includes reservations).
        requireSufficientQuantityForUpdate(userAccountId, cardId, 1);

        String decrementSql = """
                UPDATE player_collection_card
                SET quantity = quantity - 1
                WHERE user_account_id = ? AND card_id = ? AND quantity > 1
                """;
        String deleteSql = """
                DELETE FROM player_collection_card
                WHERE user_account_id = ? AND card_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement decrement = conn.prepareStatement(decrementSql);
             PreparedStatement delete = conn.prepareStatement(deleteSql)) {

            decrement.setLong(1, userAccountId);
            decrement.setLong(2, cardId);

            if (decrement.executeUpdate() == 0) {
                delete.setLong(1, userAccountId);
                delete.setLong(2, cardId);
                delete.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error removing card from player collection", e);
        }
    }

    @Override
    public void requireSufficientQuantityForUpdate(long userAccountId, long cardId, int requiredQuantity) {
        if (requiredQuantity <= 0) {
            throw new IllegalArgumentException("requiredQuantity must be positive");
        }

        // Check "available" quantity (owned - reserved).
        String ownedSql = "SELECT quantity FROM player_collection_card WHERE user_account_id = ? AND card_id = ? FOR UPDATE";
        Integer have = jdbcTemplate.query(ownedSql, rs -> rs.next() ? rs.getInt("quantity") : 0, userAccountId, cardId);
        if (have == null) {
            have = 0;
        }

        String reservedSql = """
                SELECT COALESCE(SUM(quantity), 0) AS reserved_qty
                FROM card_reservation
                WHERE user_account_id = ? AND card_id = ?
                FOR UPDATE
                """;
        Integer reserved = jdbcTemplate.query(reservedSql, rs -> rs.next() ? rs.getInt("reserved_qty") : 0, userAccountId, cardId);
        if (reserved == null) {
            reserved = 0;
        }

        int available = have - reserved;
        if (available < requiredQuantity) {
            throw new IllegalStateException("User " + userAccountId + " does not have enough available of card " + cardId);
        }
    }

    @Override
    public void requireSufficientTotalQuantityForUpdate(long userAccountId, long cardId, int requiredQuantity) {
        if (requiredQuantity <= 0) {
            throw new IllegalArgumentException("requiredQuantity must be positive");
        }

        String sql = "SELECT quantity FROM player_collection_card WHERE user_account_id = ? AND card_id = ? FOR UPDATE";
        Integer have = jdbcTemplate.query(sql, rs -> rs.next() ? rs.getInt("quantity") : 0, userAccountId, cardId);
        if (have == null) {
            have = 0;
        }
        if (have < requiredQuantity) {
            throw new IllegalStateException("User " + userAccountId + " does not have enough of card " + cardId);
        }
    }

    @Override
    public void incrementCardQuantity(long userAccountId, long cardId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        String sql = """
                INSERT INTO player_collection_card (user_account_id, card_id, quantity)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)
                """;
        jdbcTemplate.update(sql, userAccountId, cardId, quantity);
    }

    @Override
    public void decrementCardQuantity(long userAccountId, long cardId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        // Don't allow decrementing below reserved quantity.
        requireSufficientQuantityForUpdate(userAccountId, cardId, quantity);

        String updateSql = """
                UPDATE player_collection_card
                SET quantity = quantity - ?
                WHERE user_account_id = ? AND card_id = ?
                """;
        jdbcTemplate.update(updateSql, quantity, userAccountId, cardId);

        String cleanupSql = """
                DELETE FROM player_collection_card
                WHERE user_account_id = ? AND card_id = ? AND quantity <= 0
                """;
        jdbcTemplate.update(cleanupSql, userAccountId, cardId);
    }

    private Card mapRow(ResultSet rs) throws SQLException {
        Card card = new Card();
        card.setId(rs.getLong("id"));
        card.setName(rs.getString("name"));
        card.setDescription(rs.getString("description"));
        card.setCardSet(rs.getString("card_set"));

        String rarity = rs.getString("rarity");
        if (rarity != null) {
            card.setRarity(CardRarity.valueOf(rarity));
        }

        String cardType = rs.getString("card_type");
        if (cardType != null) {
            card.setCardType(CardType.valueOf(cardType));
        }

        String color = rs.getString("color");
        if (color != null) {
            card.setColor(CardColor.valueOf(color));
        }

        card.setImageUrl(rs.getString("image_url"));
        card.setMana(rs.getString("mana"));
        return card;
    }
}
