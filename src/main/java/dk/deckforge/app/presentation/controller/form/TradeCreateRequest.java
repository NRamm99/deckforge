package dk.deckforge.app.presentation.controller.form;

public record TradeCreateRequest(
        Long cardId,
        Integer quantity
) {
}

