package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.command.RegisterEventResultCommand;
import dk.deckforge.app.application.dto.EventListItemView;
import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.DeckService;
import dk.deckforge.app.application.service.EventService;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.domain.enums.DeckFormat;
import dk.deckforge.app.domain.enums.Role;
import dk.deckforge.app.domain.enums.Visibility;
import dk.deckforge.app.domain.model.Deck;
import dk.deckforge.app.domain.model.Event;
import dk.deckforge.app.domain.model.EventStatus;
import dk.deckforge.app.presentation.controller.form.EvenRegistrationRequest;
import dk.deckforge.app.presentation.controller.form.EventResultRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private ProfileService profileService;

    @Mock
    private DeckService deckService;

    @Mock
    private RedirectAttributes redirectAttributes;

    private EventController controller;

    @BeforeEach
    void setUp() {
        controller = new EventController(eventService, profileService, deckService);
    }

    @Test
    void eventsAddsUpcomingCompletedEventsAndAdminFlagToModel() {
        when(profileService.getProfileByEmail("admin@example.com")).thenReturn(adminProfile());
        when(eventService.getEventListItems()).thenReturn(List.of(
                eventItem(1L, EventStatus.OPEN),
                eventItem(2L, EventStatus.COMPLETED)
        ));

        Model model = new ExtendedModelMap();
        String view = controller.events(principal("admin@example.com"), model);

        assertThat(view).isEqualTo("events");
        assertThat(model.getAttribute("admin")).isEqualTo(true);
        assertThat(model.getAttribute("upcomingEvents")).asList().hasSize(1);
        assertThat(model.getAttribute("completedEvents")).asList().hasSize(1);
    }

    @Test
    void viewEventAddsEventDetailsAndMatchingDecksToModel() {
        Event event = event(7L, DeckFormat.STANDARD);
        Deck matchingDeck = new Deck(42L, "Standard deck", DeckFormat.STANDARD, false, Visibility.PRIVATE);
        Deck otherDeck = new Deck(42L, "Commander deck", DeckFormat.COMMANDER, false, Visibility.PRIVATE);

        when(profileService.getProfileByEmail("player@example.com")).thenReturn(userProfile());
        when(eventService.getEvent(7L)).thenReturn(event);
        when(deckService.getDecksForUser(42L)).thenReturn(List.of(matchingDeck, otherDeck));
        when(eventService.getRegistrations(7L)).thenReturn(List.of());
        when(eventService.getResult(7L)).thenReturn(Optional.empty());
        when(eventService.isUserRegistered(7L, 42L)).thenReturn(true);

        Model model = new ExtendedModelMap();
        String view = controller.viewEvent(7L, principal("player@example.com"), model);

        assertThat(view).isEqualTo("event-view");
        assertThat(model.getAttribute("event")).isSameAs(event);
        assertThat(model.getAttribute("availableDecks")).asList().containsExactly(matchingDeck);
        assertThat(model.getAttribute("currentUserRegistered")).isEqualTo(true);
        assertThat(model.getAttribute("admin")).isEqualTo(false);
    }

    @Test
    void registerForEventCallsServiceAndRedirectsToEvent() {
        when(profileService.getProfileByEmail("player@example.com")).thenReturn(userProfile());

        String redirect = controller.registerForEvent(
                7L,
                new EvenRegistrationRequest(100L),
                principal("player@example.com"),
                redirectAttributes
        );

        assertThat(redirect).isEqualTo("redirect:/events/7");
        verify(redirectAttributes).addFlashAttribute("success", "Du er tilmeldt eventet.");
        verify(eventService).registerForEvent(7L, 42L, 100L);
    }

    @Test
    void cancelRegistrationCallsServiceAndRedirectsToEvent() {
        when(profileService.getProfileByEmail("player@example.com")).thenReturn(userProfile());

        String redirect = controller.cancelRegistration(7L, principal("player@example.com"), redirectAttributes);

        assertThat(redirect).isEqualTo("redirect:/events/7");
        verify(redirectAttributes).addFlashAttribute("success", "Du er afmeldt eventet.");
        verify(eventService).cancelRegistration(7L, 42L);
    }

    @Test
    void registerResultCallsServiceForAdminAndRedirectsToEvent() {
        when(profileService.getProfileByEmail("admin@example.com")).thenReturn(adminProfile());

        String redirect = controller.registerResult(
                7L,
                new EventResultRequest(42L),
                principal("admin@example.com"),
                redirectAttributes
        );

        ArgumentCaptor<RegisterEventResultCommand> commandCaptor = ArgumentCaptor.forClass(RegisterEventResultCommand.class);
        verify(eventService).registerWinner(commandCaptor.capture());
        assertThat(commandCaptor.getValue().eventId()).isEqualTo(7L);
        assertThat(commandCaptor.getValue().winnerUserAccountId()).isEqualTo(42L);
        assertThat(commandCaptor.getValue().adminUserAccountId()).isEqualTo(1L);
        assertThat(redirect).isEqualTo("redirect:/events/7");
        verify(redirectAttributes).addFlashAttribute("success", "Resultatet er registreret.");
    }

    private static Principal principal(String email) {
        return () -> email;
    }

    private static ProfileView userProfile() {
        return new ProfileView(42L, "player@example.com", "Player", Visibility.PUBLIC, Role.USER, null);
    }

    private static ProfileView adminProfile() {
        return new ProfileView(1L, "admin@example.com", "Admin", Visibility.PUBLIC, Role.ADMIN, null);
    }

    private static Event event(long id, DeckFormat format) {
        Event event = new Event("Event " + id, "Local Store", LocalDateTime.now().plusDays(1), format, 8, EventStatus.OPEN, 1L);
        event.setId(id);
        return event;
    }

    private static EventListItemView eventItem(long id, EventStatus status) {
        return new EventListItemView(id, "Event " + id, LocalDateTime.now().plusDays(1), DeckFormat.STANDARD, status, 2, 8);
    }
}
