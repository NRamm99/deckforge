package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.UserAccount;

import java.util.Optional;

public interface UserAccountRepository {
    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(long id);

    UserAccount save(UserAccount user);

    void updateDebugFields(long id, String email, String passwordHash, dk.deckforge.app.domain.model.Role role);
}
