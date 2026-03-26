package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.dto.*;
import de.rebelmetal.schockenwebapp.model.GameParticipant;
import de.rebelmetal.schockenwebapp.model.GameSession;
import de.rebelmetal.schockenwebapp.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for game session management.
 * All business logic is delegated to GameService — this class only handles HTTP concerns.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    /**
     * Creates a new game session for the given players.
     * Players are seated in the order their IDs are provided.
     * POST /api/sessions
     */
    @PostMapping
    public ResponseEntity<GameSessionDTO> createSession(@RequestBody CreateSessionRequest request) {
        GameSession session = gameService.createSession(request.playerIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(session));
    }

    /**
     * Returns the current state of an existing game session.
     * GET /api/sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<GameSessionDTO> getSession(@PathVariable UUID sessionId) {
        GameSession session = gameService.getSession(sessionId);
        return ResponseEntity.ok(toDTO(session));
    }

    /**
     * Returns a filtered state projection of the session.
     * Dice values are hidden (null) for participants whose cup is still covered.
     * Safe for broadcasting to all clients without information leakage.
     * GET /api/sessions/{sessionId}/state
     */
    @GetMapping("/{sessionId}/state")
    public ResponseEntity<GameStateDTO> getSessionState(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(gameService.getSessionState(sessionId));
    }

    /**
     * Evaluates all rolls in the setup phase and determines the starting order.
     * On a tie, returns the tied participants and transitions to SETTING_UP_ORDER.
     * POST /api/sessions/{sessionId}/setup
     */
    @PostMapping("/{sessionId}/setup")
    public ResponseEntity<List<ParticipantDTO>> evaluateSetup(
            @PathVariable UUID sessionId,
            @RequestBody RoundEvaluationRequest request) {
        List<GameParticipant> result = gameService.evaluateSetupAndDetermineOrder(
                sessionId, request.participantIds());
        return ResponseEntity.ok(result.stream().map(this::toDTO).toList());
    }

    /**
     * Performs a virtual (randomized) dice roll for the given participant.
     * POST /api/sessions/{sessionId}/participants/{participantId}/roll
     */
    @PostMapping("/{sessionId}/participants/{participantId}/roll")
    public ResponseEntity<ParticipantDTO> performRoll(
            @PathVariable UUID sessionId,
            @PathVariable UUID participantId) {
        GameParticipant participant = gameService.performVirtualRoll(sessionId, participantId);
        return ResponseEntity.ok(toDTO(participant));
    }

    /**
     * Evaluates the current round, distributes chips, and advances the game phase.
     * POST /api/sessions/{sessionId}/evaluate
     */
    @PostMapping("/{sessionId}/evaluate")
    public ResponseEntity<RoundResultDTO> evaluateRound(
            @PathVariable UUID sessionId,
            @RequestBody RoundEvaluationRequest request) {
        RoundResultDTO result = gameService.evaluateRoundAndDistributeChips(
                sessionId, request.participantIds());
        return ResponseEntity.ok(result);
    }

    // ── DTO mapping ───────────────────────────────────────────────────────────

    private GameSessionDTO toDTO(GameSession session) {
        return new GameSessionDTO(
                session.getId(),
                session.getPhase(),
                session.getCentralStack(),
                session.getParticipants().stream().map(this::toDTO).toList()
        );
    }

    private ParticipantDTO toDTO(GameParticipant p) {
        return new ParticipantDTO(
                p.getId(),
                p.getPlayer().getName(),
                p.getPenaltyChips(),
                p.isLostFirstHalf(),
                p.isLostSecondHalf(),
                p.getLastRoll(),
                p.getThrowCount(),
                p.isSafe()
        );
    }
}
