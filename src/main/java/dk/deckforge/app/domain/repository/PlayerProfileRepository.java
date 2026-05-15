package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.PlayerProfile;

import java.util.Optional;

public interface PlayerProfileRepository {
    Optional<PlayerProfile> findByUserAccountId(long userAccountId);

    PlayerProfile save(PlayerProfile profile);

    void updateDebugFields(long userAccountId, String displayName, dk.deckforge.app.domain.model.Visibility collectionVisibility);
}
