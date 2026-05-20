CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS player_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_account_id BIGINT UNIQUE NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    collection_visibility VARCHAR(50) NOT NULL,
    CONSTRAINT fk_player_profile_user_account
        FOREIGN KEY (user_account_id)
        REFERENCES user_account(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    card_set VARCHAR(255),
    rarity VARCHAR(50),
    card_type VARCHAR(50),
    color VARCHAR(50),
    image_url VARCHAR(1024),
    mana VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS player_collection_card (
    user_account_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (user_account_id, card_id),
    CONSTRAINT fk_player_collection_user_account
        FOREIGN KEY (user_account_id)
        REFERENCES user_account(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_player_collection_card
        FOREIGN KEY (card_id)
        REFERENCES card(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS player_deck (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_account_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    format VARCHAR(50) NOT NULL,
    concept_deck BOOLEAN NOT NULL DEFAULT FALSE,
    visibility VARCHAR(50) NOT NULL DEFAULT 'PUBLIC',
    CONSTRAINT fk_player_deck_user_account
        FOREIGN KEY (user_account_id)
        REFERENCES user_account(id)
        ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS player_deck_card (
    deck_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (deck_id, card_id),
    CONSTRAINT fk_player_deck_card_deck
        FOREIGN KEY (deck_id)
        REFERENCES player_deck(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_player_deck_card_card
        FOREIGN KEY (card_id)
        REFERENCES card(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS trade_card (
    trade_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (trade_id, card_id),
    CONSTRAINT fk_trade_card_trade
        FOREIGN KEY (trade_id)
        REFERENCES trade(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_trade_card_card
        FOREIGN KEY (card_id)
        REFERENCES card(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trade_offer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_id BIGINT NOT NULL,
    offer_user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_trade_offer_trade
        FOREIGN KEY (trade_id)
        REFERENCES trade(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_trade_offer_user_account
        FOREIGN KEY (offer_user_id)
        REFERENCES user_account(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trade_offer_card (
    trade_offer_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (trade_offer_id, card_id),
    CONSTRAINT fk_trade_offer_card_offer
        FOREIGN KEY (trade_offer_id)
        REFERENCES trade_offer(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_trade_offer_card_card
        FOREIGN KEY (card_id)
        REFERENCES card(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS card_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_account_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    reservation_type VARCHAR(20) NOT NULL,
    reservation_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_card_reservation_user_card (user_account_id, card_id),
    INDEX idx_card_reservation_type_id (reservation_type, reservation_id),
    CONSTRAINT fk_card_reservation_user_account
        FOREIGN KEY (user_account_id)
        REFERENCES user_account(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_card_reservation_card
        FOREIGN KEY (card_id)
        REFERENCES card(id)
        ON DELETE CASCADE
);
