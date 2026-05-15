package dk.deckforge.app.application.service;

import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.repository.CollectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CardService cardService;

    public CollectionService(CollectionRepository collectionRepository, CardService cardService) {
        this.collectionRepository = collectionRepository;
        this.cardService = cardService;
    }

    public List<CollectionCard> getFilteredCardsForUser(long userAccountId, Card filter) {
        List<CollectionCard> collectionCards = collectionRepository.findCardsByUserAccountId(userAccountId);
        List<Card> filteredCards = cardService.filterCards(
                collectionCards.stream().map(CollectionCard::getCard).collect(Collectors.toList()),
                filter
        );

        return collectionCards.stream()
                .filter(collectionCard -> filteredCards.contains(collectionCard.getCard()))
                .collect(Collectors.toList());
    }

    public void addCardToUserCollection(long userAccountId, long cardId) {
        cardService.getCard(cardId);
        collectionRepository.addCardToUserCollection(userAccountId, cardId);
    }

    public void removeCardFromUserCollection(long userAccountId, long cardId) {
        cardService.getCard(cardId);
        collectionRepository.removeCardFromUserCollection(userAccountId, cardId);
    }
}
