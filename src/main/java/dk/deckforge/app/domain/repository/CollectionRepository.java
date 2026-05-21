package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.CollectionCard;

import java.util.List;

public interface CollectionRepository {
    List<CollectionCard> findCardsByUserAccountId(long userAccountId);

    void addCardToUserCollection(long userAccountId, long cardId);

    void removeCardFromUserCollection(long userAccountId, long cardId);

    void requireSufficientQuantityForUpdate(long userAccountId, long cardId, int requiredQuantity);

    void requireSufficientTotalQuantityForUpdate(long userAccountId, long cardId, int requiredQuantity);

    void incrementCardQuantity(long userAccountId, long cardId, int quantity);

    void decrementCardQuantity(long userAccountId, long cardId, int quantity);
}
