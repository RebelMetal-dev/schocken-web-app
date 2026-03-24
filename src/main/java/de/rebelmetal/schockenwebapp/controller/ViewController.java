package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.model.GameParticipant;
import de.rebelmetal.schockenwebapp.model.GameSession;
import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.repository.GameSessionRepository;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import de.rebelmetal.schockenwebapp.service.GameService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/**
 * Serves the Digital Tavern web interface.
 * Returns either the full page shell (initial load) or individual Thymeleaf fragments
 * for HTMX partial updates — keeping bandwidth and re-render cost minimal.
 */
@Controller
@RequiredArgsConstructor
public class ViewController {

    // Fragment name constants — single point of change if a template file is renamed.
    private static final String TEMPLATE_FULL_PAGE      = "index";
    private static final String FRAGMENT_TAVERN_TABLE   = "fragments/tavern-table :: tavern-table";
    private static final String FRAGMENT_PLAYER_SIDEBAR = "fragments/player-sidebar :: player-sidebar";

    // HTMX server-sent event name — must exactly match the hx-trigger value in player-sidebar.html.
    private static final String EVENT_DICE_ROLLED    = "diceRolled";

    // HTTP session attribute key for the active game session UUID.
    // Stored on initial load so all fragment endpoints can resolve the correct session.
    private static final String SESSION_ATTR_GAME_ID = "gameSessionId";

    private final GameService gameService;
    private final PlayerRepository playerRepository;
    private final GameSessionRepository gameSessionRepository;

    /**
     * Initial page load: seeds two test players, creates a fresh game session,
     * persists the session ID in the HTTP session, and renders the full page shell.
     */
    @GetMapping("/")
    public String index(Model model, HttpSession httpSession) {
        Player alice = playerRepository.save(new Player("Alice"));
        Player bob   = playerRepository.save(new Player("Bob"));

        // createSession() returns the entity from merge() — its proxy state can vary
        // depending on Hibernate's internal handling of pre-assigned UUIDs.
        // Re-fetching via findById() guarantees a clean, fully-initialised entity
        // backed by the current OSIV Hibernate session, consistent with all other endpoints.
        UUID sessionId = gameService.createSession(List.of(alice.getId(), bob.getId())).getId();
        httpSession.setAttribute(SESSION_ATTR_GAME_ID, sessionId);

        populateModel(model, gameSessionRepository.findById(sessionId).orElseThrow());
        return TEMPLATE_FULL_PAGE;
    }

    /**
     * Performs a virtual dice roll for the given participant.
     * Returns only the tavern-table fragment and broadcasts the "diceRolled" HTMX event
     * via a response header — the sidebar reacts independently without OOB coupling.
     */
    @PostMapping("/game/roll")
    public String roll(@RequestParam UUID participantId,
                       Model model,
                       HttpSession httpSession,
                       HttpServletResponse response) {
        UUID sessionId = (UUID) httpSession.getAttribute(SESSION_ATTR_GAME_ID);
        gameService.performVirtualRoll(sessionId, participantId);

        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();
        populateModel(model, session);

        // Signal all listening HTMX elements (the sidebar) to self-update.
        response.addHeader("HX-Trigger", EVENT_DICE_ROLLED);
        return FRAGMENT_TAVERN_TABLE;
    }

    /**
     * Returns the tavern-table fragment on direct GET.
     * Allows the client to refresh the dice area independently of a roll action.
     */
    @GetMapping("/game/table-fragment")
    public String tableFragment(Model model, HttpSession httpSession) {
        populateModel(model, fetchActiveSession(httpSession));
        return FRAGMENT_TAVERN_TABLE;
    }

    /**
     * Returns the player-sidebar fragment.
     * Called automatically by HTMX when a "diceRolled" event is received from the body.
     */
    @GetMapping("/game/sidebar-fragment")
    public String sidebarFragment(Model model, HttpSession httpSession) {
        GameSession session = fetchActiveSession(httpSession);
        model.addAttribute("participants", toViewModels(session.getParticipants()));
        return FRAGMENT_PLAYER_SIDEBAR;
    }

    // --- Private helpers ---

    private GameSession fetchActiveSession(HttpSession httpSession) {
        UUID sessionId = (UUID) httpSession.getAttribute(SESSION_ATTR_GAME_ID);
        return gameSessionRepository.findById(sessionId).orElseThrow();
    }

    private void populateModel(Model model, GameSession session) {
        model.addAttribute("game", session);
        model.addAttribute("participants", toViewModels(session.getParticipants()));
    }

    private List<ParticipantViewModel> toViewModels(List<GameParticipant> participants) {
        return participants.stream().map(ParticipantViewModel::from).toList();
    }
}
