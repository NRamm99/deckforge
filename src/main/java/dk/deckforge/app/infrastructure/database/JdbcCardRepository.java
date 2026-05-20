package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.enums.CardColor;
import dk.deckforge.app.domain.enums.CardRarity;
import dk.deckforge.app.domain.enums.CardType;
import dk.deckforge.app.domain.repository.CardRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCardRepository implements CardRepository {

    private final DataSource dataSource;

    public JdbcCardRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Card> findById(long id) {
        String sql = "SELECT * FROM card WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding card by id", e);
        }
    }

    @Override
    public List<Card> findAll() {
        String sql = "SELECT * FROM card ORDER BY id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            List<Card> cards = new ArrayList<>();
            while (rs.next()) {
                cards.add(mapRow(rs));
            }
            return cards;

        } catch (SQLException e) {
            throw new RuntimeException("Error listing cards", e);
        }
    }

    @Override
    public Card save(Card card) {
        String sql = """
                INSERT INTO card (name, description, card_set, rarity, card_type, color, image_url, mana)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, card.getName());
            ps.setString(2, card.getDescription());
            ps.setString(3, card.getCardSet());
            ps.setString(4, card.getRarity() == null ? null : card.getRarity().name());
            ps.setString(5, card.getCardType() == null ? null : card.getCardType().name());
            ps.setString(6, card.getColor() == null ? null : card.getColor().name());
            ps.setString(7, card.getImageUrl());
            ps.setString(8, card.getMana());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                card.setId(keys.getLong(1));
            }
            return card;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving card", e);
        }
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

