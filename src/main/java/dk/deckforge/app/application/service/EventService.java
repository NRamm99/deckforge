package dk.deckforge.app.application.service;

import dk.deckforge.app.application.command.CreateEventCommand;
import dk.deckforge.app.application.command.RegisterEventResultCommand;
import dk.deckforge.app.application.dto.EventListItemView;
import dk.deckforge.app.domain.model.Event;
import dk.deckforge.app.domain.model.EventRegistration;
import dk.deckforge.app.domain.model.EventRegistrationStatus;
import dk.deckforge.app.domain.model.EventResult;
import dk.deckforge.app.domain.model.EventStatus;
import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final DeckService deckService;

    public EventService(EventRepository eventRepository, DeckService deckService) {
        this.eventRepository = eventRepository;
        this.deckService = deckService;
    }

    @Transactional
    public Event createEvent(CreateEventCommand command) {
        validateEvent(command);

        Event event = new Event(
                command.title().trim(),
                command.location().trim(),
                command.dateTime(),
                command.format(),
                command.maxParticipants(),
                EventStatus.OPEN,
                command.organizerUserAccountId()
        );

        return eventRepository.save(event);
    }

    public List<EventListItemView> getEventListItems() {
        return eventRepository.findAll().stream()
                .map(this::toListItemView)
                .collect(Collectors.toList());
    }

    public Event getEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Eventet blev ikke fundet."));
    }

    public List<EventRegistration> getRegistrations(long eventId) {
        return eventRepository.findRegistrationsByEventId(eventId);
    }

    public Optional<EventResult> getResult(long eventId) {
        return eventRepository.findResultByEventId(eventId);
    }

    public boolean isUserRegistered(long eventId, long userAccountId) {
        return eventRepository.findRegistration(eventId, userAccountId)
                .filter(registration -> registration.getStatus() == EventRegistrationStatus.REGISTERED)
                .isPresent();
    }

    @Transactional
    public void registerForEvent(long eventId, long userAccountId, Long deckId) {
        Event event = getEvent(eventId);

        if (event.getStatus() != EventStatus.OPEN) {
            throw new IllegalArgumentException("Eventet er ikke åbent for tilmelding.");
        }

        if (isUserRegistered(eventId, userAccountId)) {
            throw new IllegalArgumentException("Du er allerede tilmeldt eventet.");
        }

        int registeredCount = eventRepository.countRegistered(eventId);
        if (registeredCount >= event.getMaxParticipants()) {
            throw new IllegalArgumentException("Eventet er fuldt.");
        }

        if (deckId == null || deckId <= 0) {
            throw new IllegalArgumentException("Du skal vælge et deck for at tilmelde dig.");
        }

        Deck deck = deckService.getDeckForUser(userAccountId, deckId);

        if (deck.getFormat() != event.getFormat()) {
            throw new IllegalArgumentException("Decket skal matche eventets format.");
        }

        eventRepository.saveOrReactivateRegistration(eventId, userAccountId, deckId);
    }

    @Transactional
    public void cancelRegistration(long eventId, long userAccountId) {
        Event event = getEvent(eventId);

        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Du kan ikke afmelde dig et afsluttet eller aflyst event.");
        }

        EventRegistration registration = eventRepository.findRegistration(eventId, userAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Du er ikke tilmeldt eventet."));

        if (registration.getStatus() != EventRegistrationStatus.REGISTERED) {
            throw new IllegalArgumentException("Du er ikke aktivt tilmeldt eventet.");
        }

        eventRepository.cancelRegistration(eventId, userAccountId);
    }

    @Transactional
    public void registerWinner(RegisterEventResultCommand command) {
        Event event = getEvent(command.eventId());

        if (command.winnerUserAccountId() == null || command.winnerUserAccountId() <= 0) {
            throw new IllegalArgumentException("Vælg en vinder.");
        }

        List<EventRegistration> registrations = eventRepository.findRegistrationsByEventId(event.getId());

        boolean winnerParticipated = registrations.stream()
                .anyMatch(registration -> registration.getUserAccountId() == command.winnerUserAccountId());

        if (!winnerParticipated) {
            throw new IllegalArgumentException("Vinderen skal være en tilmeldt deltager.");
        }

        EventResult result = new EventResult(
                event.getId(),
                command.winnerUserAccountId(),
                LocalDateTime.now()
        );

        eventRepository.saveOrUpdateResult(result);
        eventRepository.updateStatus(event.getId(), EventStatus.COMPLETED);
    }

    private EventListItemView toListItemView(Event event) {
        return new EventListItemView(
                event.getId(),
                event.getTitle(),
                event.getDateTime(),
                event.getFormat(),
                event.getStatus(),
                event.getRegisteredCount(),
                event.getMaxParticipants()
        );
    }

    private void validateEvent(CreateEventCommand command) {
        if (command.title() == null || command.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Titel er påkrævet.");
        }

        if (command.location() == null || command.location().trim().isEmpty()) {
            throw new IllegalArgumentException("Sted er påkrævet.");
        }

        if (command.dateTime() == null) {
            throw new IllegalArgumentException("Dato og tidspunkt er påkrævet.");
        }

        if (command.dateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Eventet kan ikke oprettes i fortiden.");
        }

        if (command.format() == null) {
            throw new IllegalArgumentException("Format er påkrævet.");
        }

        if (command.maxParticipants() == null || command.maxParticipants() <= 0) {
            throw new IllegalArgumentException("Maks antal deltagere skal være større end 0.");
        }
    }
}