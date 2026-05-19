package dk.deckforge.app.infrastructure.database;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class DeckSchemaMigrator {

    private final DataSource dataSource;

    public DeckSchemaMigrator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void addDeckVisibilityColumnIfMissing() {
        try (Connection conn = dataSource.getConnection()) {

            if (!hasTable(conn, "player_deck")) {
                return;
            }

            if (hasColumn(conn, "player_deck", "visibility")) {
                return;
            }

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(" ALTER TABLE player_deck ADD COLUMN visibility VARCHAR(50) NOT NULL DEFAULT 'PUBLIC'");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error migrating deck visibility column", e);
        }
    }

    private boolean hasTable(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }

        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }

        try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }

        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }

        try (ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return rs.next();
        }
    }
}
