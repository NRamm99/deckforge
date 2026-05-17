package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.command.CreateEventCommand;
import dk.deckforge.app.application.command.RegisterEventResultCommand;
import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.EventService;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.domain.model.DeckFormat;
import dk.deckforge.app.domain.model.Event;
import dk.deckforge.app.domain.model.Role;
import dk.deckforge.app.domain.model.EventStatus;
import dk.deckforge.app.presentation.controller.form.EventResultRequest;
import dk.deckforge.app.presentation.controller.form.EventSaveRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;

@Controller
public class EventController {

    private final EventService eventService;
    private final ProfileService profileService;

    public EventController(EventService eventService, ProfileService profileService) {
        this.eventService = eventService;
        this.profileService = profileService;
    }

    @GetMapping("/events")
    public String events(Principal principal, Model model) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        var events = eventService.getEventListItems();

        model.addAttribute("upcomingEvents", events.stream()
                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                .toList());

        model.addAttribute("completedEvents", events.stream()
                .filter(event -> event.getStatus() == EventStatus.COMPLETED)
                .toList());

        model.addAttribute("admin", isAdmin(currentProfile));
        return "events";
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable long id,
                            Principal principal,
                            Model model) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        Event event = eventService.getEvent(id);

        model.addAttribute("event", event);
        model.addAttribute("registrations", eventService.getRegistrations(id));
        model.addAttribute("result", eventService.getResult(id).orElse(null));
        model.addAttribute("currentUserRegistered", eventService.isUserRegistered(id, currentProfile.getUserId()));
        model.addAttribute("admin", isAdmin(currentProfile));

        return "event-view";
    }

    @GetMapping("/events/create")
    public String createEventPage(Principal principal,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        if (!isAdmin(currentProfile)) {
            redirectAttributes.addFlashAttribute("error", "Kun admin kan oprette events.");
            return "redirect:/events";
        }

        model.addAttribute("formats", Arrays.asList(DeckFormat.values()));
        return "event-create";
    }

    @PostMapping("/events/create")
    public String createEvent(@ModelAttribute EventSaveRequest request,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        if (!isAdmin(currentProfile)) {
            redirectAttributes.addFlashAttribute("error", "Kun admin kan oprette events.");
            return "redirect:/events";
        }

        try {
            Event event = eventService.createEvent(new CreateEventCommand(
                    currentProfile.getUserId(),
                    request.title(),
                    request.location(),
                    request.dateTime(),
                    request.format(),
                    request.maxParticipants()
            ));

            redirectAttributes.addFlashAttribute("success", "Eventet er oprettet.");
            return "redirect:/events/" + event.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/events/create";
        }
    }

    @PostMapping("/events/{id}/register")
    public String registerForEvent(@PathVariable long id,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        try {
            eventService.registerForEvent(id, currentProfile.getUserId());
            redirectAttributes.addFlashAttribute("success", "Du er tilmeldt eventet.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/cancel-registration")
    public String cancelRegistration(@PathVariable long id,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        try {
            eventService.cancelRegistration(id, currentProfile.getUserId());
            redirectAttributes.addFlashAttribute("success", "Du er afmeldt eventet.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/events/" + id;
    }

    @GetMapping("/events/{id}/result")
    public String resultPage(@PathVariable long id,
                             Principal principal,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        if (!isAdmin(currentProfile)) {
            redirectAttributes.addFlashAttribute("error", "Kun admin kan registrere resultater.");
            return "redirect:/events/" + id;
        }

        model.addAttribute("event", eventService.getEvent(id));
        model.addAttribute("registrations", eventService.getRegistrations(id));
        model.addAttribute("result", eventService.getResult(id).orElse(null));

        return "event-result";
    }

    @PostMapping("/events/{id}/result")
    public String registerResult(@PathVariable long id,
                                 @ModelAttribute EventResultRequest request,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        if (!isAdmin(currentProfile)) {
            redirectAttributes.addFlashAttribute("error", "Kun admin kan registrere resultater.");
            return "redirect:/events/" + id;
        }

        try {
            eventService.registerWinner(new RegisterEventResultCommand(
                    id,
                    request.winnerUserAccountId(),
                    currentProfile.getUserId()
            ));

            redirectAttributes.addFlashAttribute("success", "Resultatet er registreret.");
            return "redirect:/events/" + id;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/events/" + id + "/result";
        }
    }

    private boolean isAdmin(ProfileView profile) {
        return profile.getRole() == Role.ADMIN;
    }
}