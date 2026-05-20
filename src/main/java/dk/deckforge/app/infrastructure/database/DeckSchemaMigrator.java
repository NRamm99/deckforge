package dk.deckforge.app.infrastructure.database;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DeckSchemaMigrator implements ApplicationRunner {

    private final DataSource dataSource;

    public DeckSchemaMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        addDeckVisibilityColumnIfMissing();
    }

    public void addDeckVisibilityColumnIfMissing() {
        try (Connection conn = dataSource.getConnection()) {
            if (!hasTable(conn, "player_deck")) {
                return;
            }
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

    private boolean hasTable(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName.toUpperCase(), null)) {
            return rs.next();
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
