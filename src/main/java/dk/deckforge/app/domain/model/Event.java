package dk.deckforge.app.domain.model;

import java.time.LocalDateTime;

public class Event {

    private long id;
    private String title;
    private String location;
    private LocalDateTime dateTime;
    private DeckFormat format;
    private int maxParticipants;
    private EventStatus status;
    private long organizerUserAccountId;
    private int registeredCount;

    public Event() {
    }

    public Event(String title, String location, LocalDateTime dateTime, DeckFormat format, int maxParticipants, EventStatus status, long organizerUserAccountId) {
        this.title = title;
        this.location = location;
        this.dateTime = dateTime;
        this.format = format;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.organizerUserAccountId = organizerUserAccountId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public DeckFormat getFormat() {
        return format;
    }

    public void setFormat(DeckFormat format) {
        this.format = format;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public long getOrganizerUserAccountId() {
        return organizerUserAccountId;
    }

    public void setOrganizerUserAccountId(long organizerUserAccountId) {
        this.organizerUserAccountId = organizerUserAccountId;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public boolean isFull() {
        return registeredCount >= maxParticipants;
    }
}