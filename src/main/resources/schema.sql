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
