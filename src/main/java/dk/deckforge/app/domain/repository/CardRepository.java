package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.Card;

import java.util.List;
import java.util.Optional;

public interface CardRepository {
    Optional<Card> findById(long id);

    List<Card> findAll();

    Card save(Card card);
}
