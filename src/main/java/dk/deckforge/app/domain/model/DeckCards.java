package dk.deckforge.app.domain.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeckCards {

    private final Map<Long, Integer> quantities;

    private DeckCards(Map<Long, Integer> quantities) {
        this.quantities = quantities;
    }

    public static DeckCards from(Map<Long, Integer> cardQuantities) {
        if (cardQuantities == null || cardQuantities.isEmpty()) {
            throw new IllegalArgumentException("Decket har ingen kort.");
        }

        Map<Long, Integer> normalizedQuantities = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : cardQuantities.entrySet()) {
            Long cardId = entry.getKey();
            Integer quantity = entry.getValue();

            if (cardId == null) {
                throw new IllegalArgumentException("Decket indeholder et ugyldigt kort.");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Kortantal skal være større end 0.");
            }

            normalizedQuantities.put(cardId, quantity);
        }

        return new DeckCards(Collections.unmodifiableMap(normalizedQuantities));
    }

    public int totalQuantity() {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<Long, Integer> asMap() {
        return quantities;
    }

    public void validateOwnedQuantities(Map<Long, Integer> ownedQuantities) {
        Map<Long, Integer> safeOwnedQuantities = ownedQuantities == null ? Map.of() : ownedQuantities;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            int ownedQuantity = safeOwnedQuantities.getOrDefault(entry.getKey(), 0);
            if (entry.getValue() > ownedQuantity) {
                throw new IllegalArgumentException("Decket indeholder flere kopier af et kort, end du ejer.");
            }
        }
    }
}
