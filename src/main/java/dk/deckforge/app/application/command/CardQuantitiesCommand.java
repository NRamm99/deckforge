package dk.deckforge.app.application.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record CardQuantitiesCommand(Map<Long, Integer> cardQuantities) {

    public CardQuantitiesCommand {
        if (cardQuantities == null || cardQuantities.isEmpty()) {
            throw new IllegalArgumentException("At least one card is required");
        }
        cardQuantities = Collections.unmodifiableMap(new LinkedHashMap<>(cardQuantities));
    }

    public static CardQuantitiesCommand fromLists(List<Long> cardIds, List<Integer> quantities) {
        if (cardIds == null || quantities == null || cardIds.isEmpty() || quantities.isEmpty()) {
            throw new IllegalArgumentException("At least one card is required");
        }
        if (cardIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Invalid card rows");
        }

        Map<Long, Integer> aggregated = new LinkedHashMap<>();
        for (int i = 0; i < cardIds.size(); i++) {
            Long cardId = cardIds.get(i);
            Integer qty = quantities.get(i);
            if (cardId == null) {
                continue;
            }
            int q = qty == null ? 1 : qty;
            if (q <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            aggregated.merge(cardId, q, Integer::sum);
        }

        if (aggregated.isEmpty()) {
            throw new IllegalArgumentException("At least one card is required");
        }

        return new CardQuantitiesCommand(aggregated);
    }
}

