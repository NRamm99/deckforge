package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.UserAccount;
import dk.deckforge.app.domain.model.Role;

import java.util.Optional;

public interface UserAccountRepository {
    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findById(long id);

    UserAccount save(UserAccount user);

    void updateAccount(long id, String email, String passwordHash, Role role);
}
