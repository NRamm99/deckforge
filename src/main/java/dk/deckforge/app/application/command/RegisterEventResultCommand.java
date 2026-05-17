package dk.deckforge.app.application.command;

public record RegisterEventResultCommand(
        long eventId,
        Long winnerUserAccountId,
        long adminUserAccountId
) {
}