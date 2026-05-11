package dk.deckforge.app.domain.model;

public class Card {

    private long id;
    private String name;
    private String description;
    private String setName;
    private CardRarity rarity;
    private CardType cardType;
    private CardColor color;
    private String imageUrl;

    public Card() {
    }

    public Card(long id, String name, String description, String setName, CardRarity rarity, CardType cardType, CardColor color, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.setName = setName;
        this.rarity = rarity;
        this.cardType = cardType;
        this.color = color;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public CardRarity getRarity() {
        return rarity;
    }

    public void setRarity(CardRarity rarity) {
        this.rarity = rarity;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public CardColor getColor() {
        return color;
    }

    public void setColor(CardColor color) {
        this.color = color;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}