package dk.deckforge.app.infrastructure.database;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DeckSchemaMigrator {

    private final DataSource dataSource;

    public DeckSchemaMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void addDeckVisibilityColumnIfMissing() {
        try (Connection conn = dataSource.getConnection()) {
            if (hasColumn(conn, "player_deck", "visibility")) {
                return;
            }

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE player_deck ADD COLUMN visibility VARCHAR(50) NOT NULL DEFAULT 'PUBLIC'");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error migrating deck visibility column", e);
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }

        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return rs.next();
        }
    }
}
