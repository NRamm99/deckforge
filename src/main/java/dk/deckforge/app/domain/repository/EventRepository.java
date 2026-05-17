package dk.deckforge.app.domain.repository;

import dk.deckforge.app.domain.model.Event;
import dk.deckforge.app.domain.model.EventRegistration;
import dk.deckforge.app.domain.model.EventResult;
import dk.deckforge.app.domain.model.EventStatus;

import java.util.List;
import java.util.Optional;

public interface EventRepository {

    Event save(Event event);

    List<Event> findAll();

    Optional<Event> findById(long eventId);

    List<EventRegistration> findRegistrationsByEventId(long eventId);

    Optional<EventRegistration> findRegistration(long eventId, long userAccountId);

    void saveOrReactivateRegistration(long eventId, long userAccountId);

    void cancelRegistration(long eventId, long userAccountId);

    int countRegistered(long eventId);

    Optional<EventResult> findResultByEventId(long eventId);

    void saveOrUpdateResult(EventResult result);

    void updateStatus(long eventId, EventStatus Status);
}