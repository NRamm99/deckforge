package dk.deckforge.app.application.service;

import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card getCard(long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }

    @Transactional
    public void createCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card is required");
        }
        if (card.getName() == null || card.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Card name is required");
        }

        card.setName(card.getName().trim());
        if (card.getCardSet() != null) {
            card.setCardSet(card.getCardSet().trim());
        }
        if (card.getMana() != null) {
            card.setMana(card.getMana().trim());
        }
        if (card.getImageUrl() != null) {
            card.setImageUrl(card.getImageUrl().trim());
        }

        cardRepository.save(card);
    }
}
