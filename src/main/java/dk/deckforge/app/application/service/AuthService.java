package dk.deckforge.app.application.service;

import dk.deckforge.app.application.dto.AuthResponse;
import dk.deckforge.app.application.dto.LoginRequest;
import dk.deckforge.app.application.dto.RegisterRequest;
import dk.deckforge.app.application.port.PasswordHasher;
import dk.deckforge.app.domain.model.PlayerProfile;
import dk.deckforge.app.domain.enums.Role;
import dk.deckforge.app.domain.model.UserAccount;
import dk.deckforge.app.domain.enums.Visibility;
import dk.deckforge.app.domain.repository.PlayerProfileRepository;
import dk.deckforge.app.domain.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordHasher passwordHasher;

    public AuthService(UserAccountRepository userAccountRepository,
                       PlayerProfileRepository playerProfileRepository,
                       PasswordHasher passwordHasher) {
        this.userAccountRepository = userAccountRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userAccountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        UserAccount user = new UserAccount();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHasher.hash(request.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);

        UserAccount saved = userAccountRepository.save(user);

        PlayerProfile profile = new PlayerProfile();
        profile.setUserAccountId(saved.getId());
        profile.setDisplayName(request.getDisplayName());
        profile.setCollectionVisibility(Visibility.PUBLIC);
        playerProfileRepository.save(profile);

        return new AuthResponse(saved.getId(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {

        UserAccount user = userAccountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("User is deactivated");
        }

        return new AuthResponse(user.getId(), user.getEmail());
    }
}
