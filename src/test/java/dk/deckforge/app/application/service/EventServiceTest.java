package dk.deckforge.app.application.service;

import dk.deckforge.app.application.command.CreateEventCommand;
import dk.deckforge.app.application.command.RegisterEventResultCommand;
import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.enums.Visibility;
import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.model.Event;
import dk.deckforge.app.domain.model.EventRegistration;
import dk.deckforge.app.domain.model.EventRegistrationStatus;
import dk.deckforge.app.domain.model.EventResult;
import dk.deckforge.app.domain.model.EventStatus;
import dk.deckforge.app.domain.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private DeckService deckService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, deckService);
    }

    @Test
    void createEventPersistsTrimmedOpenEvent() {
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event saved = eventService.createEvent(new CreateEventCommand(
                42L,
                "  Friday Standard  ",
                "  Local Store  ",
                dateTime,
                DeckFormat.STANDARD,
                16
        ));

        assertThat(saved.getTitle()).isEqualTo("Friday Standard");
        assertThat(saved.getLocation()).isEqualTo("Local Store");
        assertThat(saved.getDateTime()).isEqualTo(dateTime);
        assertThat(saved.getFormat()).isEqualTo(DeckFormat.STANDARD);
        assertThat(saved.getStatus()).isEqualTo(EventStatus.OPEN);
        assertThat(saved.getOrganizerUserAccountId()).isEqualTo(42L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEventRejectsBlankTitle() {
        assertThatThrownBy(() -> eventService.createEvent(new CreateEventCommand(
                42L,
                " ",
                "Local Store",
                LocalDateTime.now().plusDays(1),
                DeckFormat.STANDARD,
                16
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Titel er p\u00e5kr\u00e6vet.");

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void registerForEventSavesRegistrationWhenEventAndDeckAreValid() {
        when(eventRepository.findById(7L)).thenReturn(Optional.of(event(7L, EventStatus.OPEN, DeckFormat.STANDARD)));
        when(eventRepository.findRegistration(7L, 42L)).thenReturn(Optional.empty());
        when(eventRepository.countRegistered(7L)).thenReturn(2);
        when(deckService.getDeckForUser(42L, 100L))
                .thenReturn(new Deck(42L, "Standard deck", DeckFormat.STANDARD, false, Visibility.PRIVATE));

        eventService.registerForEvent(7L, 42L, 100L);

        verify(eventRepository).saveOrReactivateRegistration(7L, 42L, 100L);
    }

    @Test
    void registerWinnerStoresResultAndCompletesEvent() {
        when(eventRepository.findById(7L)).thenReturn(Optional.of(event(7L, EventStatus.OPEN, DeckFormat.STANDARD)));
        when(eventRepository.findRegistrationsByEventId(7L))
                .thenReturn(List.of(registration(7L, 42L)));

        eventService.registerWinner(new RegisterEventResultCommand(7L, 42L, 1L));

        ArgumentCaptor<EventResult> resultCaptor = ArgumentCaptor.forClass(EventResult.class);
        verify(eventRepository).saveOrUpdateResult(resultCaptor.capture());
        assertThat(resultCaptor.getValue().getEventId()).isEqualTo(7L);
        assertThat(resultCaptor.getValue().getWinnerUserAccountId()).isEqualTo(42L);
        assertThat(resultCaptor.getValue().getCreatedAt()).isNotNull();
        verify(eventRepository).updateStatus(7L, EventStatus.COMPLETED);
    }

    private static Event event(long id, EventStatus status, DeckFormat format) {
        Event event = new Event(
                "Event " + id,
                "Local Store",
                LocalDateTime.now().plusDays(1),
                format,
                8,
                status,
                1L
        );
        event.setId(id);
        return event;
    }

    private static EventRegistration registration(long eventId, long userAccountId) {
        EventRegistration registration = new EventRegistration();
        registration.setEventId(eventId);
        registration.setUserAccountId(userAccountId);
        registration.setStatus(EventRegistrationStatus.REGISTERED);
        return registration;
    }
}
