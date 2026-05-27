package dk.deckforge.app.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventResultTest {

    @Test
    void eventResultStoresConstructorValues() {
        LocalDateTime createdAt = LocalDateTime.now();

        EventResult result = new EventResult(7L, 42L, createdAt);

        assertThat(result.getEventId()).isEqualTo(7L);
        assertThat(result.getWinnerUserAccountId()).isEqualTo(42L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void eventResultStoresUpdatedValues() {
        LocalDateTime createdAt = LocalDateTime.now();
        EventResult result = new EventResult();

        result.setEventId(8L);
        result.setWinnerUserAccountId(43L);
        result.setCreatedAt(createdAt);
        result.setWinnerDisplayName("Winner");

        assertThat(result.getEventId()).isEqualTo(8L);
        assertThat(result.getWinnerUserAccountId()).isEqualTo(43L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getWinnerDisplayName()).isEqualTo("Winner");
    }
}
