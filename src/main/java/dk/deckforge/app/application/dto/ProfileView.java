package dk.deckforge.app.application.dto;

import dk.deckforge.app.domain.model.Visibility;

public class ProfileView {

    private String email;
    private String displayName;
    private Visibility collectionVisibility;

    public ProfileView(String email, String displayName, Visibility collectionVisibility) {
        this.email = email;
        this.displayName = displayName;
        this.collectionVisibility = collectionVisibility;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Visibility getCollectionVisibility() {
        return collectionVisibility;
    }
}
