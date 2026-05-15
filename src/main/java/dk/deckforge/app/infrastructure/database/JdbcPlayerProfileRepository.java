package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.PlayerProfile;
import dk.deckforge.app.domain.model.Visibility;
import dk.deckforge.app.domain.repository.PlayerProfileRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class JdbcPlayerProfileRepository implements PlayerProfileRepository {

    private final DataSource dataSource;

    public JdbcPlayerProfileRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<PlayerProfile> findByUserAccountId(long userAccountId) {
        String sql = "SELECT * FROM player_profile WHERE user_account_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userAccountId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding player profile by user account id", e);
        }
    }

    @Override
    public PlayerProfile save(PlayerProfile profile) {
        String sql = """
                INSERT INTO player_profile (user_account_id, display_name, collection_visibility)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, profile.getUserAccountId());
            ps.setString(2, profile.getDisplayName());
            ps.setString(3, profile.getCollectionVisibility().name());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                profile.setId(keys.getLong(1));
            }

            return profile;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving player profile", e);
        }
    }

    @Override
    public void updateProfile(long userAccountId, String displayName, Visibility collectionVisibility) {
        String sql = """
                UPDATE player_profile
                SET display_name = ?, collection_visibility = ?
                WHERE user_account_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, displayName);
            ps.setString(2, collectionVisibility.name());
            ps.setLong(3, userAccountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating player profile", e);
        }
    }

    private PlayerProfile mapRow(ResultSet rs) throws SQLException {
        PlayerProfile profile = new PlayerProfile();
        profile.setId(rs.getLong("id"));
        profile.setUserAccountId(rs.getLong("user_account_id"));
        profile.setDisplayName(rs.getString("display_name"));
        profile.setCollectionVisibility(Visibility.valueOf(rs.getString("collection_visibility")));
        return profile;
    }
}
