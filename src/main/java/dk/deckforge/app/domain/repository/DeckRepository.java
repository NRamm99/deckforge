package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.model.CollectionCard;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DeckRepository {

    Deck save(Deck deck, Map<Long, Integer> cardQuantities);

    List<Deck> findByUserAccountId(long userAccountId);

    Optional<Deck> findByIdAndUserAccountId(long deckId, long userAccountId);

    List<CollectionCard> findCardsByDeckId(long deckId);

    void deleteByIdAndUserAccountId(long deckId, long userAccountId);
}
