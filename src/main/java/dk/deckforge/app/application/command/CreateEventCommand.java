package dk.deckforge.app.application.command;

import dk.deckforge.app.domain.model.DeckFormat;

import java.time.LocalDateTime;

public record CreateEventCommand(
        long organizerUserAccountId,
        String title,
        String location,
        LocalDateTime dateTime,
        DeckFormat format,
        Integer maxParticipants
) {
}