package dk.deckforge.app.application.service;

import dk.deckforge.app.application.command.CreateDeckCommand;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.model.DeckCards;
import dk.deckforge.app.domain.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final CollectionService collectionService;

    public DeckService(DeckRepository deckRepository, CollectionService collectionService) {
        this.deckRepository = deckRepository;
        this.collectionService = collectionService;
    }

    @Transactional
    public Deck saveDeck(CreateDeckCommand command) {
        DeckCards deckCards = DeckCards.from(command.cardQuantities());
        Map<Long, Integer> ownedQuantities = command.conceptDeck() ? Map.of() : ownedQuantitiesForUser(command.userAccountId());
        Deck deck = Deck.create(
                command.userAccountId(),
                command.name(),
                command.format(),
                command.conceptDeck(),
                command.visibility(),
                deckCards,
                ownedQuantities
        );

        return deckRepository.save(deck, deckCards.asMap());
    }

    public List<Deck> getDecksForUser(long userAccountId) {
        return deckRepository.findByUserAccountId(userAccountId);
    }

    public Deck getDeckForUser(long userAccountId, long deckId) {
        return deckRepository.findByIdAndUserAccountId(deckId, userAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));
    }

    public List<CollectionCard> getDeckCards(long deckId) {
        return deckRepository.findCardsByDeckId(deckId);
    }

    public void deleteDeck(long userAccountId, long deckId) {
        getDeckForUser(userAccountId, deckId);
        deckRepository.deleteByIdAndUserAccountId(deckId, userAccountId);
    }

    private Map<Long, Integer> ownedQuantitiesForUser(long userAccountId) {
        return collectionService.getFilteredCardsForUser(userAccountId, null).stream()
                .collect(Collectors.toMap(collectionCard -> collectionCard.getCard().getId(), CollectionCard::getQuantity));
    }
}
