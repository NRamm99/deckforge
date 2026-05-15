package dk.deckforge.app.application.command;

import dk.deckforge.app.domain.model.Role;
import dk.deckforge.app.domain.model.Visibility;

public record UpdateDebugProfileCommand(
        String currentEmail,
        String email,
        String displayName,
        String password,
        Role role,
        Visibility collectionVisibility
) {
}
