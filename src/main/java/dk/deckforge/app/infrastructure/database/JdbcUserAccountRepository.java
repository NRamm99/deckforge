package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.Role;
import dk.deckforge.app.domain.model.UserAccount;
import dk.deckforge.app.domain.repository.UserAccountRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class JdbcUserAccountRepository implements UserAccountRepository {

    private final DataSource dataSource;

    public JdbcUserAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        String sql = "SELECT * FROM user_account WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UserAccount user = mapRow(rs);
                return Optional.of(user);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    @Override
    public Optional<UserAccount> findById(long id) {
        String sql = "SELECT * FROM user_account WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UserAccount user = mapRow(rs);
                return Optional.of(user);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
    }

    @Override
    public UserAccount save(UserAccount user) {
        String sql = "INSERT INTO user_account (email, password_hash, role, active) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getLong(1));
            }

            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public void updateDebugFields(long id, String email, String passwordHash, Role role) {
        String sql = passwordHash == null || passwordHash.isBlank()
                ? "UPDATE user_account SET email = ?, role = ? WHERE id = ?"
                : "UPDATE user_account SET email = ?, password_hash = ?, role = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            if (passwordHash == null || passwordHash.isBlank()) {
                ps.setString(2, role.name());
                ps.setLong(3, id);
            } else {
                ps.setString(2, passwordHash);
                ps.setString(3, role.name());
                ps.setLong(4, id);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating debug user fields", e);
        }
    }

    private UserAccount mapRow(ResultSet rs) throws SQLException {
        UserAccount user = new UserAccount();

        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("active"));

        return user;
    }
}
