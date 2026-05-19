package dk.deckforge.app.presentation.controller;

import dk.deckforge.app.application.command.CardQuantitiesCommand;
import dk.deckforge.app.application.dto.ProfileView;
import dk.deckforge.app.application.service.CollectionService;
import dk.deckforge.app.application.service.ProfileService;
import dk.deckforge.app.application.service.TradeCommandService;
import dk.deckforge.app.application.service.TradeService;
import dk.deckforge.app.domain.model.CollectionCard;
import dk.deckforge.app.domain.model.Trade;
import dk.deckforge.app.domain.model.TradeOffer;
import dk.deckforge.app.domain.model.TradeStatus;
import dk.deckforge.app.presentation.web.SafeRedirect;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class TradeController {

    private final TradeService tradeService;
    private final TradeCommandService tradeCommandService;
    private final ProfileService profileService;
    private final CollectionService collectionService;

    public TradeController(TradeService tradeService,
                           TradeCommandService tradeCommandService,
                           ProfileService profileService,
                           CollectionService collectionService) {
        this.tradeService = tradeService;
        this.tradeCommandService = tradeCommandService;
        this.profileService = profileService;
        this.collectionService = collectionService;
    }

    @GetMapping("/trades")
    public String trades(Model model, Principal principal) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());
        model.addAttribute("currentProfile", currentProfile);
        model.addAttribute("trades", tradeService.listOpenTrades());
        return "trades";
    }

    @GetMapping("/trades/new")
    public String createTradeForm(Model model, Principal principal) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());
        List<CollectionCard> collectionCards = collectionService.getFilteredCardsForUser(currentProfile.getUserId(), null);

        model.addAttribute("currentProfile", currentProfile);
        model.addAttribute("collectionCards", collectionCards);
        return "trade-create";
    }

    @PostMapping("/trades/new")
    public String createTradeSubmit(@RequestParam("cardIds") List<Long> cardIds,
                                    @RequestParam("quantities") List<Integer> quantities,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        try {
            CardQuantitiesCommand offeredCards = CardQuantitiesCommand.fromLists(cardIds, quantities);
            Trade created = tradeCommandService.createOpenTrade(currentProfile.getUserId(), offeredCards);
            redirectAttributes.addFlashAttribute("success", "Trade oprettet.");
            return "redirect:/trades/" + created.getId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/trades/new";
        }
    }

    @GetMapping("/trades/{tradeId}")
    public String viewTrade(@PathVariable long tradeId, Principal principal, Model model) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());
        Trade trade = tradeService.getTrade(tradeId);
        List<TradeOffer> offers = tradeService.listOffersForTrade(tradeId);
        List<CollectionCard> collectionCards = collectionService.getFilteredCardsForUser(currentProfile.getUserId(), null);

        boolean ownTrade = currentProfile.getUserId() == trade.getCreatorUserId();
        boolean open = trade.getStatus() == TradeStatus.OPEN;

        model.addAttribute("currentProfile", currentProfile);
        model.addAttribute("trade", trade);
        model.addAttribute("offers", offers);
        model.addAttribute("ownTrade", ownTrade);
        model.addAttribute("openTrade", open);
        model.addAttribute("collectionCards", collectionCards);
        return "trade-view";
    }

    @PostMapping("/trades/{tradeId}/offer")
    public String createOffer(@PathVariable long tradeId,
                              @RequestParam("cardIds") List<Long> cardIds,
                              @RequestParam("quantities") List<Integer> quantities,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        try {
            CardQuantitiesCommand offeredCards = CardQuantitiesCommand.fromLists(cardIds, quantities);
            tradeCommandService.createOffer(tradeId, currentProfile.getUserId(), offeredCards);
            redirectAttributes.addFlashAttribute("success", "Bud sendt.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/trades/" + tradeId;
    }

    @PostMapping("/trades/{tradeId}/offers/{offerId}/accept")
    public String acceptOffer(@PathVariable long tradeId,
                              @PathVariable long offerId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        try {
            tradeService.acceptOffer(tradeId, offerId, currentProfile.getUserId());
            redirectAttributes.addFlashAttribute("success", "Bud accepteret.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/trades/" + tradeId;
    }

    @PostMapping("/trades/{tradeId}/cancel")
    public String cancelTrade(@PathVariable long tradeId,
                              Principal principal,
                              RedirectAttributes redirectAttributes,
                              @RequestHeader(value = "Referer", required = false) String referer) {
        ProfileView currentProfile = profileService.getProfileByEmail(principal.getName());

        try {
            tradeService.cancelTrade(tradeId, currentProfile.getUserId());
            redirectAttributes.addFlashAttribute("success", "Trade annulleret.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:" + SafeRedirect.byPathPrefix(referer, "/trades/", "/trades/" + tradeId);
    }
}
