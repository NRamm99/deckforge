package dk.deckforge.app.application.service;

import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.domain.model.PlayerProfile;
import dk.deckforge.app.domain.model.UserAccount;
import dk.deckforge.app.domain.repository.PlayerProfileRepository;
import dk.deckforge.app.domain.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserAccountRepository userAccountRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public ProfileService(UserAccountRepository userAccountRepository,
                          PlayerProfileRepository playerProfileRepository) {
        this.userAccountRepository = userAccountRepository;
        this.playerProfileRepository = playerProfileRepository;
    }

    public ProfileView getProfileByEmail(String email) {
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return getProfileForUser(user);
    }

    public ProfileView getProfileByUserId(long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return getProfileForUser(user);
    }

    private ProfileView getProfileForUser(UserAccount user) {
        PlayerProfile profile = playerProfileRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        return new ProfileView(
                user.getEmail(),
                profile.getDisplayName(),
                profile.getCollectionVisibility()
        );
    }
}
