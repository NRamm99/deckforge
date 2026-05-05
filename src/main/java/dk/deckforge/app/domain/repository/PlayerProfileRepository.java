package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.PlayerProfile;

import java.util.Optional;

public interface PlayerProfileRepository {
    Optional<PlayerProfile> findByUserAccountId(long userAccountId);

    PlayerProfile save(PlayerProfile profile);
}
