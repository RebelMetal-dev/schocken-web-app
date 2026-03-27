package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.dto.RoundResultDTO;
import de.rebelmetal.schockenwebapp.model.GameParticipant;
import de.rebelmetal.schockenwebapp.model.GamePhase;
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
import java.util.stream.IntStream;

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

    // HTMX server-sent event names — must exactly match hx-trigger values in fragment templates.
    private static final String EVENT_DICE_ROLLED    = "diceRolled";
    private static final String EVENT_ROUND_EVALUATED = "roundEvaluated";

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
     * Evaluates the setup rolls and determines the starting order.
     * Transitions phase from WAITING_FOR_PLAYERS (or SETTING_UP_ORDER) to FIRST_HALF.
     * All participants must have rolled before calling this.
     */
    @PostMapping("/game/start")
    public String startGame(Model model,
                            HttpSession httpSession,
                            HttpServletResponse response) {
        UUID sessionId = (UUID) httpSession.getAttribute(SESSION_ATTR_GAME_ID);
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();

        List<UUID> participantIds = session.getParticipants().stream()
                .map(GameParticipant::getId).toList();

        gameService.evaluateSetupAndDetermineOrder(sessionId, participantIds);

        populateModel(model, gameSessionRepository.findById(sessionId).orElseThrow());
        response.addHeader("HX-Trigger", EVENT_DICE_ROLLED);
        return FRAGMENT_TAVERN_TABLE;
    }

    /**
     * Evaluates the current round and distributes chips.
     * Participant IDs are resolved server-side from the active session —
     * the client does not need to track or submit them.
     * Returns the updated tavern-table fragment and broadcasts "roundEvaluated"
     * so the sidebar self-refreshes with the new chip counts.
     */
    @PostMapping("/game/evaluate")
    public String evaluate(Model model,
                           HttpSession httpSession,
                           HttpServletResponse response) {
        UUID sessionId = (UUID) httpSession.getAttribute(SESSION_ATTR_GAME_ID);
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();

        List<UUID> participantIds = session.getParticipants().stream()
                .map(GameParticipant::getId).toList();

        RoundResultDTO result = gameService.evaluateRoundAndDistributeChips(sessionId, participantIds);

        // Re-fetch after evaluation: phase and centralStack have changed.
        populateModel(model, gameSessionRepository.findById(sessionId).orElseThrow());
        model.addAttribute("roundResult", result);

        response.addHeader("HX-Trigger", EVENT_ROUND_EVALUATED);
        return FRAGMENT_TAVERN_TABLE;
    }

    /**
     * Ends the active player's turn and advances the turn pointer.
     * Sets the roll limit if this is the starter's first finish-turn call.
     */
    @PostMapping("/game/finish-turn")
    public String finishTurn(@RequestParam UUID participantId,
                             Model model,
                             HttpSession httpSession,
                             HttpServletResponse response) {
        UUID sessionId = (UUID) httpSession.getAttribute(SESSION_ATTR_GAME_ID);
        gameService.finishTurn(sessionId, participantId);

        populateModel(model, gameSessionRepository.findById(sessionId).orElseThrow());
        response.addHeader("HX-Trigger", EVENT_DICE_ROLLED);
        return FRAGMENT_TAVERN_TABLE;
    }

    /**
     * Reveals the active player's cup.
     * If Blind-Zwang applies (player exhausted roll limit), 1 penalty chip is issued
     * by the service and lastRoll is frozen to firstRoll.
     */
    @PostMapping("/game/reveal-cup")
    public String revealCup(@RequestParam UUID participantId,
                            Model model,
                            HttpSession httpSession,
                            HttpServletResponse response) {
        UUID sessionId = (UUID) httpSession.getAttribute(SESSION_ATTR_GAME_ID);
        gameService.revealCup(sessionId, participantId);

        populateModel(model, gameSessionRepository.findById(sessionId).orElseThrow());
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
        model.addAttribute("participants",
                toViewModels(session.getParticipants(), session.getActiveParticipantIndex()));
        return FRAGMENT_PLAYER_SIDEBAR;
    }

    // --- Private helpers ---

    private GameSession fetchActiveSession(HttpSession httpSession) {
        UUID sessionId = (UUID) httpSession.getAttribute(SESSION_ATTR_GAME_ID);
        return gameSessionRepository.findById(sessionId).orElseThrow();
    }

    private void populateModel(Model model, GameSession session) {
        model.addAttribute("game", session);
        model.addAttribute("participants",
                toViewModels(session.getParticipants(), session.getActiveParticipantIndex()));
        // canEvaluate: semantic boolean — template decides button visibility, not CSS strings.
        model.addAttribute("canEvaluate", isEvaluatablePhase(session.getPhase()));
        model.addAttribute("canStart", isSetupPhase(session.getPhase()));
    }

    private boolean isEvaluatablePhase(GamePhase phase) {
        return phase == GamePhase.FIRST_HALF
                || phase == GamePhase.SECOND_HALF
                || phase == GamePhase.FINAL_MATCH;
    }

    private boolean isSetupPhase(GamePhase phase) {
        return phase == GamePhase.WAITING_FOR_PLAYERS
                || phase == GamePhase.SETTING_UP_ORDER;
    }

    private List<ParticipantViewModel> toViewModels(List<GameParticipant> participants, int activeIndex) {
        return IntStream.range(0, participants.size())
                .mapToObj(i -> ParticipantViewModel.from(participants.get(i), i == activeIndex))
                .toList();
    }
}
