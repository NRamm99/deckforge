package dk.deckforge.app.presentation.controller.form;

import dk.deckforge.app.domain.model.DeckFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record EventSaveRequest(
        String title,
        String location,

        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime dateTime,

        DeckFormat format,
        Integer maxParticipants
) {
}