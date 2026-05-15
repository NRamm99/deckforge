package dk.deckforge.app.application.service;

import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.model.DeckFormat;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Visibility;
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
    public Deck saveDeck(long userAccountId, String name, DeckFormat format, boolean conceptDeck, Visibility visibility, Map<Long, Integer> cardQuantities) {
        validateDeck(userAccountId, name, format, conceptDeck, cardQuantities);

        Deck deck = new Deck(userAccountId, name.trim(), format, conceptDeck, visibility == null ? Visibility.PUBLIC : visibility);
        return deckRepository.save(deck, cardQuantities);
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

    private void validateDeck(long userAccountId, String name, DeckFormat format, boolean conceptDeck, Map<Long, Integer> cardQuantities) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Decknavn er påkrævet.");
        }
        if (format == null) {
            throw new IllegalArgumentException("Vælg et format.");
        }

        if (cardQuantities == null) {
            throw new IllegalArgumentException("Decket har ingen kort.");
        }

        int deckSize = cardQuantities.values().stream().mapToInt(Integer::intValue).sum();
        if (deckSize < format.getMinDeckSize() || deckSize > format.getMaxDeckSize()) {
            throw new IllegalArgumentException(format.name() + " kræver " + format.getMinDeckSize()
                    + "-" + format.getMaxDeckSize() + " kort. Decket har " + deckSize + ".");
        }

        if (!conceptDeck) {
            validateOwnedQuantities(userAccountId, cardQuantities);
        }
    }

    private void validateOwnedQuantities(long userAccountId, Map<Long, Integer> cardQuantities) {
        Map<Long, Integer> ownedQuantities = collectionService.getFilteredCardsForUser(userAccountId, null).stream()
                .collect(Collectors.toMap(collectionCard -> collectionCard.getCard().getId(), CollectionCard::getQuantity));

        for (Map.Entry<Long, Integer> entry : cardQuantities.entrySet()) {
            int ownedQuantity = ownedQuantities.getOrDefault(entry.getKey(), 0);
            if (entry.getValue() > ownedQuantity) {
                throw new IllegalArgumentException("Decket indeholder flere kopier af et kort, end du ejer.");
            }
        }
    }
}
