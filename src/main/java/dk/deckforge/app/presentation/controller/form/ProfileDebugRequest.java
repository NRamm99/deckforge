package dk.deckforge.app.presentation.controller.form;

import dk.deckforge.app.domain.enums.Role;
import dk.deckforge.app.domain.enums.Visibility;

public record ProfileDebugRequest(
        String email,
        String displayName,
        String password,
        Role role,
        Visibility collectionVisibility
) {
}
