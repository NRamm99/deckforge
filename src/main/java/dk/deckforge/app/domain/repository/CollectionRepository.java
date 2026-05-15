package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.CollectionCard;

import java.util.List;

public interface CollectionRepository {
    List<CollectionCard> findCardsByUserAccountId(long userAccountId);

    void addCardToUserCollection(long userAccountId, long cardId);

    void removeCardFromUserCollection(long userAccountId, long cardId);
}
