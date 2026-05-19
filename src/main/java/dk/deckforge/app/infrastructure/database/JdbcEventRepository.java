package dk.deckforge.app.infrastructure.database;

import dk.deckforge.app.domain.model.DeckFormat;
import dk.deckforge.app.domain.model.Event;
import dk.deckforge.app.domain.model.EventRegistration;
import dk.deckforge.app.domain.model.EventRegistrationStatus;
import dk.deckforge.app.domain.model.EventResult;
import dk.deckforge.app.domain.model.EventStatus;
import dk.deckforge.app.domain.repository.EventRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEventRepository implements EventRepository {

    private final DataSource dataSource;

    public JdbcEventRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    //Events
    @Override
    public Event save(Event event) {
        String sql = """
                INSERT INTO deckforge_event
                    (title, location, date_time, format, max_participants, status, organizer_user_account_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, event.getTitle());
            ps.setString(2, event.getLocation());
            ps.setTimestamp(3, Timestamp.valueOf(event.getDateTime()));
            ps.setString(4, event.getFormat().name());
            ps.setInt(5, event.getMaxParticipants());
            ps.setString(6, event.getStatus().name());
            ps.setLong(7, event.getOrganizerUserAccountId());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                event.setId(keys.getLong(1));
            }

            return event;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving event", e);
        }
    }

    @Override
    public List<Event> findAll() {
        String sql = """
                SELECT e.*,
                       COUNT(CASE WHEN er.status = 'REGISTERED' THEN 1 END) AS registered_count
                FROM deckforge_event e
                LEFT JOIN event_registration er ON er.event_id = e.id
                GROUP BY e.id, e.title, e.location, e.date_time, e.format, e.max_participants, e.status, e.organizer_user_account_id
                ORDER BY e.date_time ASC
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            List<Event> events = new ArrayList<>();

            while (rs.next()) {
                events.add(mapEvent(rs));
            }

            return events;
        } catch (SQLException e) {
            throw new RuntimeException("Error listing events", e);
        }
    }

    @Override
    public Optional<Event> findById(long eventId) {
        String sql = """
                SELECT e.*,
                       COUNT(CASE WHEN er.status = 'REGISTERED' THEN 1 END) AS registered_count
                FROM deckforge_event e
                LEFT JOIN event_registration er ON er.event_id = e.id
                WHERE e.id = ?
                GROUP BY e.id, e.title, e.location, e.date_time, e.format, e.max_participants, e.status, e.organizer_user_account_id
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapEvent(rs));
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding event", e);
        }
    }

    @Override
    public void updateStatus(long eventId, EventStatus status) {
        String sql = """
            UPDATE deckforge_event
            SET status = ?
            WHERE id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setLong(2, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating event status", e);
        }
    }



    //Registrations
    @Override
    public List<EventRegistration> findRegistrationsByEventId(long eventId) {
        String sql = """
                SELECT registration.event_id,
                       registration.user_account_id,
                       registration.deck_id,
                       registration.status,
                       registration.registered_at,
                       profile.display_name,
                       account.email,
                       deck.name AS deck_name
                FROM event_registration registration
                JOIN user_account account ON account.id = registration.user_account_id
                JOIN player_profile profile ON profile.user_account_id = account.id
                LEFT JOIN player_deck deck ON deck.id = registration.deck_id
                WHERE registration.event_id = ?
                  AND registration.status = 'REGISTERED'
                ORDER BY registration.registered_at ASC
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            ResultSet rs = ps.executeQuery();
            List<EventRegistration> registrations = new ArrayList<>();

            while (rs.next()) {
                registrations.add(mapRegistration(rs));
            }

            return registrations;
        } catch (SQLException e) {
            throw new RuntimeException("Error listing event registrations", e);
        }
    }

    @Override
    public Optional<EventRegistration> findRegistration(long eventId, long userAccountId) {
        String sql = """
                SELECT registration.event_id,
                       registration.user_account_id,
                       registration.deck_id,
                       registration.status,
                       registration.registered_at,
                       profile.display_name,
                       account.email,
                       deck.name AS deck_name
                FROM event_registration registration
                JOIN user_account account ON account.id = registration.user_account_id
                JOIN player_profile profile ON profile.user_account_id = account.id
                LEFT JOIN player_deck deck ON deck.id = registration.deck_id
                WHERE registration.event_id = ?
                  AND registration.user_account_id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userAccountId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRegistration(rs));
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding event registration", e);
        }
    }

    @Override
    public void saveOrReactivateRegistration(long eventId, long userAccountId, long deckId) {
        String sql = """
            INSERT INTO event_registration (event_id, user_account_id, deck_id, status, registered_at)
            VALUES (?, ?, ?, 'REGISTERED', NOW())
            ON DUPLICATE KEY UPDATE
                deck_id = VALUES(deck_id),
                status = 'REGISTERED',
                registered_at = NOW()
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userAccountId);
            ps.setLong(3, deckId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error registering for event", e);
        }
    }

    @Override
    public void cancelRegistration(long eventId, long userAccountId) {
        String sql = """
                UPDATE event_registration
                SET status = 'CANCELLED'
                WHERE event_id = ?
                  AND user_account_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userAccountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error cancelling event registration", e);
        }
    }

    @Override
    public int countRegistered(long eventId) {
        String sql = """
                SELECT COUNT(*) AS registered_count
                FROM event_registration
                WHERE event_id = ?
                  AND status = 'REGISTERED'
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("registered_count");
            }

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting event registrations", e);
        }
    }


    //Results
    @Override
    public Optional<EventResult> findResultByEventId(long eventId) {
        String sql = """
               SELECT result.event_id,
                      result.winner_user_account_id,
                      result.created_at,
                      profile.display_name AS winner_display_name
               FROM event_result result
               JOIN user_account account ON account.id = result.winner_user_account_id
               JOIN player_profile profile ON profile.user_account_id = account.id
               WHERE result.event_id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResult(rs));
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding event result", e);
        }
    }

    @Override
    public void saveOrUpdateResult(EventResult result) {
        String sql = """
                INSERT INTO event_result (event_id, winner_user_account_id, created_at)
                VALUES (?, ?, NOW())
                ON DUPLICATE KEY UPDATE
                    winner_user_account_id = VALUES(winner_user_account_id),
                    created_at = NOW()
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, result.getEventId());
            ps.setLong(2, result.getWinnerUserAccountId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving event result", e);
        }
    }


    //Mapping
    private Event mapEvent(ResultSet rs) throws SQLException {
        Event event = new Event();

        event.setId(rs.getLong("id"));
        event.setTitle(rs.getString("title"));
        event.setLocation(rs.getString("location"));
        event.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        event.setFormat(DeckFormat.valueOf(rs.getString("format")));
        event.setMaxParticipants(rs.getInt("max_participants"));
        event.setStatus(EventStatus.valueOf(rs.getString("status")));
        event.setOrganizerUserAccountId(rs.getLong("organizer_user_account_id"));
        event.setRegisteredCount(rs.getInt("registered_count"));

        return event;
    }

    private EventRegistration mapRegistration(ResultSet rs) throws SQLException {
        EventRegistration registration = new EventRegistration();

        registration.setEventId(rs.getLong("event_id"));
        registration.setUserAccountId(rs.getLong("user_account_id"));

        long deckId = rs.getLong("deck_id");
        if (!rs.wasNull()) {
            registration.setDeckId(deckId);
        }

        registration.setStatus(EventRegistrationStatus.valueOf(rs.getString("status")));
        registration.setRegisteredAt(rs.getTimestamp("registered_at").toLocalDateTime());
        registration.setDisplayName(rs.getString("display_name"));
        registration.setEmail(rs.getString("email"));
        registration.setDeckName(rs.getString("deck_name"));

        return registration;
    }

    private EventResult mapResult(ResultSet rs) throws SQLException {
        EventResult result = new EventResult();

        result.setEventId(rs.getLong("event_id"));
        result.setWinnerUserAccountId(rs.getLong("winner_user_account_id"));
        result.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        result.setWinnerDisplayName(rs.getString("winner_display_name"));

        return result;
    }
}