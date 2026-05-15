package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.model.DeckFormat;
import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.model.CardColor;
import dk.deckforge.app.domain.model.CardRarity;
import dk.deckforge.app.domain.model.CardType;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Visibility;
import dk.deckforge.app.domain.repository.DeckRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcDeckRepository implements DeckRepository {

    private final DataSource dataSource;

    public JdbcDeckRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Deck save(Deck deck, Map<Long, Integer> cardQuantities) {
        String deckSql = """
                INSERT INTO player_deck (user_account_id, name, format, concept_deck, visibility)
                VALUES (?, ?, ?, ?, ?)
                """;
        String cardSql = """
                INSERT INTO player_deck_card (deck_id, card_id, quantity)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement deckStatement = conn.prepareStatement(deckSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement cardStatement = conn.prepareStatement(cardSql)) {

                deckStatement.setLong(1, deck.getUserAccountId());
                deckStatement.setString(2, deck.getName());
                deckStatement.setString(3, deck.getFormat().name());
                deckStatement.setBoolean(4, deck.isConceptDeck());
                deckStatement.setString(5, deck.getVisibility().name());
                deckStatement.executeUpdate();

                ResultSet keys = deckStatement.getGeneratedKeys();
                if (!keys.next()) {
                    throw new SQLException("Deck id was not generated");
                }

                long deckId = keys.getLong(1);
                deck.setId(deckId);

                for (Map.Entry<Long, Integer> entry : cardQuantities.entrySet()) {
                    if (entry.getValue() <= 0) {
                        continue;
                    }
                    cardStatement.setLong(1, deckId);
                    cardStatement.setLong(2, entry.getKey());
                    cardStatement.setInt(3, entry.getValue());
                    cardStatement.addBatch();
                }
                cardStatement.executeBatch();

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return deck;
            } catch (SQLException e) {
                conn.rollback();
                conn.setAutoCommit(previousAutoCommit);
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving deck", e);
        }
    }

    @Override
    public List<Deck> findByUserAccountId(long userAccountId) {
        String sql = """
                SELECT d.id, d.user_account_id, d.name, d.format, d.concept_deck, d.visibility,
                       COALESCE(SUM(pdc.quantity), 0) AS card_count
                FROM player_deck d
                LEFT JOIN player_deck_card pdc ON pdc.deck_id = d.id
                WHERE d.user_account_id = ?
                GROUP BY d.id, d.user_account_id, d.name, d.format, d.concept_deck, d.visibility
                ORDER BY d.id DESC
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userAccountId);

            ResultSet rs = ps.executeQuery();
            List<Deck> decks = new ArrayList<>();
            while (rs.next()) {
                decks.add(mapDeck(rs));
            }
            return decks;
        } catch (SQLException e) {
            throw new RuntimeException("Error listing player decks", e);
        }
    }

    @Override
    public Optional<Deck> findByIdAndUserAccountId(long deckId, long userAccountId) {
        String sql = """
                SELECT d.id, d.user_account_id, d.name, d.format, d.concept_deck, d.visibility,
                       COALESCE(SUM(pdc.quantity), 0) AS card_count
                FROM player_deck d
                LEFT JOIN player_deck_card pdc ON pdc.deck_id = d.id
                WHERE d.id = ? AND d.user_account_id = ?
                GROUP BY d.id, d.user_account_id, d.name, d.format, d.concept_deck, d.visibility
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, deckId);
            ps.setLong(2, userAccountId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapDeck(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding player deck", e);
        }
    }

    @Override
    public List<CollectionCard> findCardsByDeckId(long deckId) {
        String sql = """
                SELECT c.*, pdc.quantity
                FROM player_deck_card pdc
                JOIN card c ON c.id = pdc.card_id
                WHERE pdc.deck_id = ?
                ORDER BY c.name
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, deckId);

            ResultSet rs = ps.executeQuery();
            List<CollectionCard> cards = new ArrayList<>();
            while (rs.next()) {
                cards.add(new CollectionCard(mapCard(rs), rs.getInt("quantity")));
            }
            return cards;
        } catch (SQLException e) {
            throw new RuntimeException("Error listing deck cards", e);
        }
    }

    @Override
    public void deleteByIdAndUserAccountId(long deckId, long userAccountId) {
        String sql = """
                DELETE FROM player_deck
                WHERE id = ? AND user_account_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, deckId);
            ps.setLong(2, userAccountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting deck", e);
        }
    }

    private Deck mapDeck(ResultSet rs) throws SQLException {
        Deck deck = new Deck();
        deck.setId(rs.getLong("id"));
        deck.setUserAccountId(rs.getLong("user_account_id"));
        deck.setName(rs.getString("name"));
        deck.setFormat(DeckFormat.valueOf(rs.getString("format")));
        deck.setConceptDeck(rs.getBoolean("concept_deck"));
        deck.setVisibility(Visibility.valueOf(rs.getString("visibility")));
        deck.setCardCount(rs.getInt("card_count"));
        return deck;
    }

    private Card mapCard(ResultSet rs) throws SQLException {
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
