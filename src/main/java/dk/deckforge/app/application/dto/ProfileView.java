package dk.deckforge.app.application.dto;

import dk.deckforge.app.domain.model.Role;
import dk.deckforge.app.domain.model.Visibility;

public class ProfileView {

    private long userId;
    private String email;
    private String displayName;
    private Visibility collectionVisibility;
    private Role role;
    private String avatarUrl;

    public ProfileView(long userId, String email, String displayName, Visibility collectionVisibility, Role role, String avatarUrl) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.collectionVisibility = collectionVisibility;
        this.role = role;
        this.avatarUrl = avatarUrl;

    }

    public long getUserId() {
        return userId;
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

    public Role getRole() {
        return role;
    }

    public String getRoleLabel() {
        if (role == Role.ADMIN) {
            return "Administrator";
        }
        return "Bruger";
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
