package dk.deckforge.app.application.dto;

import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.model.EventStatus;

import java.time.LocalDateTime;

public class EventListItemView {

    private final long id;
    private final String title;
    private final LocalDateTime dateTime;
    private final DeckFormat format;
    private final EventStatus status;
    private final int registeredCount;
    private final int maxParticipants;

    public EventListItemView(long id, String title, LocalDateTime dateTime, DeckFormat format, EventStatus status,
                             int registeredCount, int maxParticipants) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.format = format;
        this.status = status;
        this.registeredCount = registeredCount;
        this.maxParticipants = maxParticipants;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public DeckFormat getFormat() {
        return format;
    }

    public EventStatus getStatus() {
        return status;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public boolean isFull() {
        return registeredCount >= maxParticipants;
    }
}