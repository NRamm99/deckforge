package dk.deckforge.app.domain.model;

import dk.deckforge.app.domain.enums.Visibility;

public class PlayerProfile {

    private long id;
    private long userAccountId;
    private String displayName;
    private Visibility collectionVisibility;
    private String avatarUrl;

    public PlayerProfile() {
    }

    public long getId() {
        return id;
    }

    public long getUserAccountId() {
        return userAccountId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Visibility getCollectionVisibility() {
        return collectionVisibility;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUserAccountId(long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setCollectionVisibility(Visibility collectionVisibility) {
        this.collectionVisibility = collectionVisibility;
    }

    public String getAvatarUrl() {return avatarUrl;}

    public void setAvatarUrl(String avatarUrl) {this.avatarUrl = avatarUrl;}
}
