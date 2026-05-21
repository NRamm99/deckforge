package dk.deckforge.app.application.command;

import dk.deckforge.app.domain.enums.Role;
import dk.deckforge.app.domain.enums.Visibility;

public record UpdateDebugProfileCommand(
        String currentEmail,
        String email,
        String displayName,
        String password,
        Role role,
        Visibility collectionVisibility
) {
}
