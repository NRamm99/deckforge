package dk.deckforge.app.presentation.controller.form;

import dk.deckforge.app.domain.model.Role;
import dk.deckforge.app.domain.model.Visibility;

public record ProfileDebugRequest(
        String email,
        String displayName,
        String password,
        Role role,
        Visibility collectionVisibility
) {
}
