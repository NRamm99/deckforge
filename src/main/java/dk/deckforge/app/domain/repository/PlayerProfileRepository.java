package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.PlayerProfile;
import dk.deckforge.app.domain.model.Visibility;

import java.util.Optional;

public interface PlayerProfileRepository {
    Optional<PlayerProfile> findByUserAccountId(long userAccountId);

    PlayerProfile save(PlayerProfile profile);

    void updateProfile(long userAccountId, String displayName, Visibility collectionVisibility);
}
