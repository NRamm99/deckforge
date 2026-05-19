package dk.deckforge.app.domain.model;

import java.time.LocalDateTime;

public class EventResult {

    private long eventId;
    private long winnerUserAccountId;
    private LocalDateTime createdAt;

    private String winnerDisplayName;

    public EventResult() {
    }

    public EventResult(long eventId, long winnerUserAccountId, LocalDateTime createdAt) {
        this.eventId = eventId;
        this.winnerUserAccountId = winnerUserAccountId;
        this.createdAt = createdAt;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getWinnerUserAccountId() {
        return winnerUserAccountId;
    }

    public void setWinnerUserAccountId(long winnerUserAccountId) {
        this.winnerUserAccountId = winnerUserAccountId;
    }

    //hvis vi vil vise hvornår resultat er registreret
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getWinnerDisplayName() {
        return winnerDisplayName;
    }

    public void setWinnerDisplayName(String winnerDisplayName) {
        this.winnerDisplayName = winnerDisplayName;
    }
}