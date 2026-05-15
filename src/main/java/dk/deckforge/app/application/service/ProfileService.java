package dk.deckforge.app.application.service;

import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.domain.model.PlayerProfile;
import dk.deckforge.app.domain.model.Role;
import dk.deckforge.app.domain.model.UserAccount;
import dk.deckforge.app.domain.model.Visibility;
import dk.deckforge.app.domain.repository.PlayerProfileRepository;
import dk.deckforge.app.domain.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserAccountRepository userAccountRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserAccountRepository userAccountRepository,
                          PlayerProfileRepository playerProfileRepository,
                          PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordEncoder = passwordEncoder;
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

    @Transactional
    public ProfileView updateDebugProfile(String currentEmail,
                                          String email,
                                          String displayName,
                                          String password,
                                          Role role,
                                          Visibility collectionVisibility) {
        UserAccount user = userAccountRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String normalizedEmail = requireText(email, "Email is required");
        String normalizedDisplayName = requireText(displayName, "Name is required");
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (collectionVisibility == null) {
            throw new IllegalArgumentException("Visibility is required");
        }

        userAccountRepository.findByEmail(normalizedEmail)
                .filter(existing -> existing.getId() != user.getId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Email already in use");
                });

        String passwordHash = password == null || password.isBlank() ? null : passwordEncoder.encode(password);
        userAccountRepository.updateDebugFields(user.getId(), normalizedEmail, passwordHash, role);
        playerProfileRepository.updateDebugFields(user.getId(), normalizedDisplayName, collectionVisibility);

        return getProfileByUserId(user.getId());
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private ProfileView getProfileForUser(UserAccount user) {
        PlayerProfile profile = playerProfileRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        return new ProfileView(
                user.getId(),
                user.getEmail(),
                profile.getDisplayName(),
                profile.getCollectionVisibility(),
                user.getRole()
        );
    }
}
