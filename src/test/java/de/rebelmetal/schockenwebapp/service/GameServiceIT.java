package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.GameParticipant;
import de.rebelmetal.schockenwebapp.model.GamePhase;
import de.rebelmetal.schockenwebapp.model.GameSession;
import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.repository.GameSessionRepository;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for GameService.
 * Verifies the full round evaluation and chip distribution flow
 * with a real Spring context and in-memory database.
 */
@SpringBootTest
@Transactional
class GameServiceIT {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Test
    @DisplayName("4-player round: Shock 2 wins, House Number loses, 2 chips distributed from stack")
    void shouldDistributeChipsCorrectlyInFourPlayerRound() {
        // --- ARRANGE ---

        // 1. Create 4 players (no explicit UUID — JPA generates it on INSERT)
        Player alice   = playerRepository.save(new Player("Alice"));
        Player bob     = playerRepository.save(new Player("Bob"));
        Player charlie = playerRepository.save(new Player("Charlie"));
        Player diana   = playerRepository.save(new Player("Diana"));

        // 2. Create session (initial central stack = 13)
        GameSession session = gameService.createSession(
                List.of(alice.getId(), bob.getId(), charlie.getId(), diana.getId()));

        // 3. Resolve participant IDs — order reflects play order (Alice rolled first)
        UUID pidAlice   = getParticipantId(session, alice.getId());
        UUID pidBob     = getParticipantId(session, bob.getId());
        UUID pidCharlie = getParticipantId(session, charlie.getId());
        UUID pidDiana   = getParticipantId(session, diana.getId());

        // 4. Assign rolls:
        //    Alice   → Shock 2   (1-1-2) → SHOCK rank,        penalty = 2  ← WINNER
        //    Bob     → Triplet   (4-4-4) → TRIPLET rank
        //    Charlie → Straight  (3-4-5) → STRAIGHT rank
        //    Diana   → House Num (6-5-3) → HOUSE_NUMBER rank               ← LOSER
        //    Note: 6-5-4 would be a Straight — 6-5-3 is the correct House Number here.
        gameService.performManualRoll(session.getId(), pidAlice,   1, 1, 2, true, 1);
        gameService.performManualRoll(session.getId(), pidBob,     4, 4, 4, true, 1);
        gameService.performManualRoll(session.getId(), pidCharlie, 3, 4, 5, true, 1);
        gameService.performManualRoll(session.getId(), pidDiana,   6, 5, 3, true, 1);

        // --- ACT ---
        // Play order passed explicitly — essential for LIFO/FIFO tie-breaking
        gameService.evaluateRoundAndDistributeChips(
                session.getId(), List.of(pidAlice, pidBob, pidCharlie, pidDiana));

        // --- ASSERT ---
        GameSession result = gameSessionRepository.findById(session.getId()).orElseThrow();

        // Central stack: started at 13, penalty = 2 → must be 11
        assertEquals(11, result.getCentralStack(),
                "Central stack should decrease from 13 to 11 (Alice's Shock 2 penalty = 2).");

        // Loser (Diana, House Number) must receive exactly 2 chips
        GameParticipant diana_result = result.getParticipants().stream()
                .filter(p -> p.getId().equals(pidDiana))
                .findFirst()
                .orElseThrow();
        assertEquals(2, diana_result.getPenaltyChips(),
                "Diana (House Number 6-5-3) should receive exactly 2 penalty chips.");

        // Phase must NOT have changed — stack still has chips, no Shock Out
        assertNotEquals(GamePhase.SECOND_HALF, result.getPhase(),
                "Phase must not transition to SECOND_HALF while the central stack is not empty.");
    }

    /** Resolves the participant ID for a given player within a session. */
    private UUID getParticipantId(GameSession session, UUID playerId) {
        return session.getParticipants().stream()
                .filter(p -> p.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Player not found in session: " + playerId))
                .getId();
    }
}
