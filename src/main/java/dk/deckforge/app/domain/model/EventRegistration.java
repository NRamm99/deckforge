package dk.deckforge.app.domain.model;

import java.time.LocalDateTime;

public class EventRegistration {

    private long eventId;
    private long userAccountId;
    private EventRegistrationStatus status;
    private LocalDateTime registeredAt;
    private String displayName;
    private String email;
    private Long deckId;
    private String deckName;

    public EventRegistration() {
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public EventRegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(EventRegistrationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDeckId() {
        return deckId;
    }

    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }

    public String getDeckName() {
        return deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }
}