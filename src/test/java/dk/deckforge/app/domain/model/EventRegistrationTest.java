package dk.deckforge.app.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventRegistrationTest {

    @Test
    void eventRegistrationStoresEventUserDeckAndStatus() {
        LocalDateTime registeredAt = LocalDateTime.now();
        EventRegistration registration = new EventRegistration();

        registration.setEventId(7L);
        registration.setUserAccountId(42L);
        registration.setDeckId(100L);
        registration.setStatus(EventRegistrationStatus.REGISTERED);
        registration.setRegisteredAt(registeredAt);

        assertThat(registration.getEventId()).isEqualTo(7L);
        assertThat(registration.getUserAccountId()).isEqualTo(42L);
        assertThat(registration.getDeckId()).isEqualTo(100L);
        assertThat(registration.getStatus()).isEqualTo(EventRegistrationStatus.REGISTERED);
        assertThat(registration.getRegisteredAt()).isEqualTo(registeredAt);
    }

    @Test
    void eventRegistrationAllowsNullableDeckId() {
        EventRegistration registration = new EventRegistration();

        registration.setDeckId(null);

        assertThat(registration.getDeckId()).isNull();
    }

    @Test
    void eventRegistrationStoresDisplayFields() {
        EventRegistration registration = new EventRegistration();

        registration.setDisplayName("Player One");
        registration.setEmail("player@example.com");
        registration.setDeckName("Standard deck");

        assertThat(registration.getDisplayName()).isEqualTo("Player One");
        assertThat(registration.getEmail()).isEqualTo("player@example.com");
        assertThat(registration.getDeckName()).isEqualTo("Standard deck");
    }
}
