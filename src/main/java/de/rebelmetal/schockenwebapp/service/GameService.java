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

    private GameParticipant resolveParticipant(UUID sessionId, UUID participantId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        return session.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + participantId));
    }
}
