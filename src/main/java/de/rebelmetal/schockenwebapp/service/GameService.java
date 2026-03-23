package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.exception.PlayerNotFoundException;
import de.rebelmetal.schockenwebapp.model.DiceRoll;
import de.rebelmetal.schockenwebapp.model.GameParticipant;
import de.rebelmetal.schockenwebapp.model.GamePhase;
import de.rebelmetal.schockenwebapp.model.GameSession;
import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.repository.GameParticipantRepository;
import de.rebelmetal.schockenwebapp.repository.GameSessionRepository;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service orchestrating the game logic for Schocken sessions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private static final int INITIAL_CENTRAL_STACK = 13;

    private final GameSessionRepository gameSessionRepository;
    private final GameParticipantRepository gameParticipantRepository;
    private final PlayerRepository playerRepository;
    private final DiceService diceService;
    private final RoundEvaluator roundEvaluator;

    /**
     * Creates a new game session with a list of players.
     * Ensures all players exist and initializes the session state.
     *
     * @param playerIds List of UUIDs representing the players.
     * @return The saved GameSession entity.
     * @throws IllegalArgumentException if less than 2 players are provided.
     * @throws PlayerNotFoundException if any ID does not match an existing player.
     */
    @Transactional
    public GameSession createSession(List<UUID> playerIds) {
        // 1. Validation: Schocken requires at least two participants
        if (playerIds == null || playerIds.size() < 2) {
            throw new IllegalArgumentException("A session must have at least 2 players!");
        }

        // 2. Initialize Session
        GameSession session = new GameSession();
        session.setId(UUID.randomUUID());
        session.setPhase(GamePhase.WAITING_FOR_PLAYERS);
        session.setCentralStack(INITIAL_CENTRAL_STACK);
        session.setParticipants(new ArrayList<>());

        // 3. Fetch players in bulk to minimize database roundtrips
        List<Player> players = playerRepository.findAllById(playerIds);

        // 4. Integrity Check: Did we find everyone?
        if (players.size() != playerIds.size()) {
            throw new PlayerNotFoundException("Some player IDs were not found in the database.");
        }

        // 5. Create GameParticipants (the join-entity holding transient game state)
        List<GameParticipant> participants = players.stream()
                .map(player -> {
                    GameParticipant p = new GameParticipant();
                    p.setId(UUID.randomUUID());
                    p.setPlayer(player);
                    p.setSession(session);
                    p.setPenaltyChips(0);
                    p.setSafe(false);
                    p.setBlind(false);
                    return p;
                })
                .toList();

        session.setParticipants(participants);

        log.info("New GameSession created with ID: {} and {} players.", session.getId(), participants.size());

        // 6. Persistence: Cascade will save participants automatically
        return gameSessionRepository.save(session);
    }

    /**
     * Performs a virtual (randomised) dice roll for a participant.
     *
     * @param sessionId     The active game session.
     * @param participantId The participant who is rolling.
     * @param hand          True if all three dice were thrown in a single attempt.
     * @param throwCount    Number of throws used (1–3).
     * @return The updated GameParticipant.
     */
    @Transactional
    public GameParticipant performVirtualRoll(UUID sessionId, UUID participantId,
                                              boolean hand, int throwCount) {
        GameParticipant participant = resolveParticipant(sessionId, participantId);
        DiceRoll roll = diceService.rollVirtually(hand, throwCount);
        participant.setLastRoll(roll);
        participant.setThrowCount(throwCount);
        gameParticipantRepository.save(participant);
        log.info("Participant '{}' rolled virtually: {}", participant.getPlayer().getName(), roll);
        return participant;
    }

    /**
     * Registers a manually entered dice roll for a participant.
     *
     * @param sessionId     The active game session.
     * @param participantId The participant who is rolling.
     * @param d1            Value of die 1.
     * @param d2            Value of die 2.
     * @param d3            Value of die 3.
     * @param hand          True if all three dice were thrown in a single attempt.
     * @param throwCount    Number of throws used (1–3).
     * @return The updated GameParticipant.
     */
    @Transactional
    public GameParticipant performManualRoll(UUID sessionId, UUID participantId,
                                             int d1, int d2, int d3,
                                             boolean hand, int throwCount) {
        GameParticipant participant = resolveParticipant(sessionId, participantId);
        DiceRoll roll = diceService.rollManually(d1, d2, d3, hand, throwCount);
        participant.setLastRoll(roll);
        participant.setThrowCount(throwCount);
        gameParticipantRepository.save(participant);
        log.info("Participant '{}' rolled manually: {}", participant.getPlayer().getName(), roll);
        return participant;
    }

    /**
     * Evaluates the outcome of a round between two participants.
     * Determines the loser via RoundEvaluator and distributes chips based on the winner's roll.
     */
    @Transactional
    public void evaluateRoundAndDistributeChips(UUID sessionId, UUID participant1Id, UUID participant2Id) {
        // 1. Load the session once - this instance will be used to resolve participants
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // 2. Resolve participants directly from the loaded session's list to avoid redundant DB calls
        GameParticipant p1 = session.getParticipants().stream()
                .filter(p -> p.getId().equals(participant1Id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participant 1 not found in session"));

        GameParticipant p2 = session.getParticipants().stream()
                .filter(p -> p.getId().equals(participant2Id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participant 2 not found in session"));

        // 3. Identify the loser using the RoundEvaluator (passing them as a list)
        List<GameParticipant> roundParticipants = List.of(p1, p2);
        GameParticipant loser = roundEvaluator.findLoser(roundParticipants);
        GameParticipant winner = (loser == p1) ? p2 : p1;

        // 4. The winner's roll determines the penalty value
        DiceRoll winningRoll = winner.getLastRoll();
        int penalty = winningRoll.getPenaltyValue();

        log.info("Evaluation for session {}: Winner {} (Roll: {}), Loser {}. Penalty: {} chips",
                sessionId, winner.getPlayer().getName(), winningRoll, loser.getPlayer().getName(), penalty);

        // 5. Handle chip distribution and half-time state transitions
        if (winningRoll.isShockOut()) {
            handleShockOut(session, loser);
            // A Shock Out by the winner immediately ends the current half for the loser
            session.setPhase(GamePhase.SECOND_HALF);
        } else {
            distributeChips(session, winner, loser, penalty);

            // If the central stack is now empty, transition to the next game phase
            if (session.getCentralStack() == 0) {
                session.setPhase(GamePhase.SECOND_HALF);
            }
        }

        // 6. Persist changes (Cascade will handle participants)
        gameSessionRepository.save(session);
    }

    private GameParticipant resolveParticipant(UUID sessionId, UUID participantId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        return session.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + participantId));
    }

    private void distributeChips(GameSession session, GameParticipant winner, GameParticipant loser, int amount) {
        // Step A: Take chips from the central stack first
        int fromStack = Math.min(session.getCentralStack(), amount);
        session.setCentralStack(session.getCentralStack() - fromStack);
        loser.setPenaltyChips(loser.getPenaltyChips() + fromStack);

        // Step B: If penalty is not satisfied, take from winner (Phase 2)
        int remaining = amount - fromStack;
        if (remaining > 0) {
            if (winner.getPenaltyChips() > 0) {
                int stolen = Math.min(winner.getPenaltyChips(), remaining);
                winner.setPenaltyChips(winner.getPenaltyChips() - stolen);
                loser.setPenaltyChips(loser.getPenaltyChips() + stolen);
                log.info("Phase 2: {} chips moved from winner to loser", stolen);
            } else {
                // Safety log for edge cases where no chips are available anywhere
                log.warn("Penalty of {} chips could not be fully distributed. No chips left.", remaining);
            }
        }
    }

    private void handleShockOut(GameSession session, GameParticipant loser) {
        // On Shock Out, the loser takes everything that's left in the middle
        int remaining = session.getCentralStack();
        loser.setPenaltyChips(loser.getPenaltyChips() + remaining);
        session.setCentralStack(0);
        log.info("SHOCK OUT! Loser {} takes all {} remaining chips.", loser.getPlayer().getName(), remaining);
    }
}
