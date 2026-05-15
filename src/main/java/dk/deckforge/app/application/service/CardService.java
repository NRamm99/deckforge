package dk.deckforge.app.application.service;

import dk.deckforge.app.domain.model.Card;
import dk.deckforge.app.domain.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public List<Card> filterCards(List<Card> cards, Card filter) {
        if (cards == null || filter == null || !hasAnyFilter(filter)) {
            return cards;
        }

        return cards.stream()
                .filter(c -> matchesFilter(c, filter))
                .collect(Collectors.toList());
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

    private boolean hasAnyFilter(Card filter) {
        return notBlank(filter.getName())
                || notBlank(filter.getCardSet())
                || filter.getRarity() != null
                || filter.getCardType() != null
                || filter.getColor() != null
                || notBlank(filter.getMana());
    }

    private boolean matchesFilter(Card card, Card filter) {
        if (card == null) return false;

        if (notBlank(filter.getName()) && doesNotContainIgnoreCase(card.getName(), filter.getName())) return false;
        if (notBlank(filter.getCardSet()) && doesNotContainIgnoreCase(card.getCardSet(), filter.getCardSet())) return false;
        if (filter.getRarity() != null && card.getRarity() != filter.getRarity()) return false;
        if (filter.getCardType() != null && card.getCardType() != filter.getCardType()) return false;
        if (filter.getColor() != null && card.getColor() != filter.getColor()) return false;
        return !notBlank(filter.getMana()) || !doesNotContainIgnoreCase(card.getMana(), filter.getMana());
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean doesNotContainIgnoreCase(String haystack, String needle) {
        if (haystack == null) return true;
        if (needle == null) return false;
        return !haystack.toLowerCase(Locale.ROOT).contains(needle.trim().toLowerCase(Locale.ROOT));
    }
}
