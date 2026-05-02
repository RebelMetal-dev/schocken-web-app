package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.dto.RoundResultDTO;
import de.rebelmetal.schockenwebapp.model.*;
import de.rebelmetal.schockenwebapp.repository.GameSessionRepository;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end integration tests for the setup phase (Auswürfeln) and the
 * first regular round. Covers the five checkpoints from the manual E2E plan:
 *
 *   1. Auto-transition to FIRST_HALF after the last setup roll via performVirtualRoll()
 *   2. Null-guard: IllegalStateException when a participant has not yet rolled
 *   3. Chips distributed from the central stack (not from players) in the first half
 *   4. Loser of a round becomes the next starter (activeParticipantIndex)
 *   5. Full flow: setup → roll → finish turn → evaluate → verify state
 *
 * Key naming convention used throughout:
 *   aliceParticipantId / bobParticipantId  — GameParticipant UUIDs (used by all service calls)
 *   alicePlayerId      / bobPlayerId       — Player UUIDs      (used only for session creation)
 *
 * These are different entities with different UUIDs. Confusing them was the
 * root cause of all 5 failures in the previous test run (EntityNotFoundException).
 */
@SpringBootTest
@Transactional
class SetupPhaseE2ETest {

    @Autowired private GameService            gameService;
    @Autowired private GameSessionRepository  gameSessionRepository;
    @Autowired private PlayerRepository       playerRepository;

    private UUID sessionId;
    private UUID aliceParticipantId;
    private UUID bobParticipantId;

    @BeforeEach
    void setUp() {
        // Step 1: create Player entities → Player UUIDs
        Player alice = playerRepository.save(new Player("Alice"));
        Player bob   = playerRepository.save(new Player("Bob"));

        // Step 2: create session using Player UUIDs
        GameSession session = gameService.createSession(
                List.of(alice.getId(), bob.getId()));
        sessionId = session.getId();

        // Step 3: resolve the GameParticipant UUIDs that the service layer uses.
        // GameParticipant.id != Player.id — they are separate entities.
        // All service calls (performVirtualRoll, finishTurn, …) require participant IDs.
        aliceParticipantId = participantByName(session, "Alice").getId();
        bobParticipantId   = participantByName(session, "Bob").getId();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Checkpoint 1 — Auto-transition via performVirtualRoll()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Phase auto-advances to FIRST_HALF after the last setup roll")
    void performVirtualRoll_lastSetupRoll_autoTransitionsToFirstHalf() {
        // Alice rolls first — only one of two players has rolled
        gameService.performVirtualRoll(sessionId, aliceParticipantId);

        GameSession afterAlice = gameSessionRepository.findById(sessionId).orElseThrow();
        assertThat(afterAlice.getPhase())
                .as("Phase must still be WAITING_FOR_PLAYERS after only Alice has rolled")
                .isEqualTo(GamePhase.WAITING_FOR_PLAYERS);

        // Bob rolls — this is the last setup roll, auto-transition must fire
        gameService.performVirtualRoll(sessionId, bobParticipantId);

        // ✅ Checkpoint 1
        GameSession afterBob = gameSessionRepository.findById(sessionId).orElseThrow();
        assertThat(afterBob.getPhase())
                .as("Phase must be FIRST_HALF after all participants have rolled in setup")
                .isEqualTo(GamePhase.FIRST_HALF);

        // Sanity: lastRolls cleared, rollLimit reset for round 1
        assertThat(afterBob.getParticipants())
                .as("All lastRolls must be cleared after setup evaluation")
                .allMatch(p -> p.getLastRoll() == null);
        assertThat(afterBob.getRollLimit())
                .as("rollLimit must be 0 at the start of round 1")
                .isEqualTo(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Checkpoint 2 — Null-guard
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("evaluateSetupAndDetermineOrder throws when a participant has not rolled")
    void evaluateSetup_withMissingRoll_throwsIllegalStateException() {
        // Only Alice has a roll — Bob's lastRoll is still null
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();
        participantById(session, aliceParticipantId)
                .setLastRoll(new DiceRoll(6, 5, 3, false, 1));
        gameSessionRepository.save(session);

        // ✅ Checkpoint 2: descriptive exception instead of silent NPE
        assertThatThrownBy(() ->
                gameService.evaluateSetupAndDetermineOrder(
                        sessionId, List.of(aliceParticipantId, bobParticipantId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Bob");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Checkpoint 3 — Chips come from central stack in FIRST_HALF
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Chips are taken from the central stack in FIRST_HALF, not from players")
    void firstHalfRound_chipsDistributedFromCentralStack() {
        // Fast-forward through setup
        gameService.performVirtualRoll(sessionId, aliceParticipantId);
        gameService.performVirtualRoll(sessionId, bobParticipantId);

        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();
        assertThat(session.getPhase()).isEqualTo(GamePhase.FIRST_HALF);

        // Chip counts are captured as primitives here — immune to JPA first-level-cache mutation.
        // The entity object returned by findById() is the same instance the service will modify,
        // so re-fetching later gives the post-eval values. Primitives are value-copied at capture time.
        int stackBefore      = session.getCentralStack();
        int aliceChipsBefore = participantById(session, aliceParticipantId).getPenaltyChips();
        int bobChipsBefore   = participantById(session, bobParticipantId).getPenaltyChips();

        // Alice wins (TRIPLET → penalty 3), Bob loses (HOUSE_NUMBER)
        participantById(session, aliceParticipantId)
                .setLastRoll(new DiceRoll(4, 4, 4, false, 1));
        participantById(session, bobParticipantId)
                .setLastRoll(new DiceRoll(6, 5, 3, false, 1));
        gameSessionRepository.save(session);

        gameService.evaluateRoundAndDistributeChips(
                sessionId, List.of(aliceParticipantId, bobParticipantId));

        GameSession after = gameSessionRepository.findById(sessionId).orElseThrow();

        // ✅ Checkpoint 3a: stack reduced by penalty (TRIPLET = 3)
        assertThat(after.getCentralStack())
                .as("Central stack must decrease by the penalty value (3 for TRIPLET)")
                .isEqualTo(stackBefore - 3);

        // ✅ Checkpoint 3b: Bob (loser) received exactly 3 more chips (TRIPLET penalty)
        // Setup may have already given Bob some chips (random rolls) — assert delta, not absolute value.
        assertThat(participantById(after, bobParticipantId).getPenaltyChips())
                .as("Loser must receive exactly 3 chips from the central stack (TRIPLET penalty)")
                .isEqualTo(bobChipsBefore + 3);

        // ✅ Checkpoint 3c: Alice's chips unchanged (stack had enough, no player-to-player transfer)
        assertThat(participantById(after, aliceParticipantId).getPenaltyChips())
                .as("Winner's chip count must not change when the stack has chips")
                .isEqualTo(aliceChipsBefore);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Checkpoint 4 — Loser becomes next starter (activeParticipantIndex)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Round loser becomes the starter of the next round")
    void afterRoundEvaluation_loserIsNextActiveParticipant() {
        // Fast-forward through setup
        gameService.performVirtualRoll(sessionId, aliceParticipantId);
        gameService.performVirtualRoll(sessionId, bobParticipantId);

        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();

        // Alice wins (TRIPLET), Bob loses (HOUSE_NUMBER)
        participantById(session, aliceParticipantId)
                .setLastRoll(new DiceRoll(4, 4, 4, false, 1));
        participantById(session, bobParticipantId)
                .setLastRoll(new DiceRoll(6, 5, 3, false, 1));
        gameSessionRepository.save(session);

        gameService.evaluateRoundAndDistributeChips(
                sessionId, List.of(aliceParticipantId, bobParticipantId));

        GameSession after = gameSessionRepository.findById(sessionId).orElseThrow();

        // ✅ Checkpoint 4: Bob (loser) is the new active participant (Pitter-Rule §2c)
        UUID nextStarterId = after.getParticipants()
                .get(after.getActiveParticipantIndex()).getId();

        assertThat(nextStarterId)
                .as("Round loser must be the starter of the next round")
                .isEqualTo(bobParticipantId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Checkpoint 5 — Full flow: setup → roll → finish turn → evaluate
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Full flow: setup auto-transitions, round completes, state is consistent")
    void fullFlow_setupToFirstRound_stateIsConsistentThroughout() {
        // ── SETUP ──
        gameService.performVirtualRoll(sessionId, aliceParticipantId);
        gameService.performVirtualRoll(sessionId, bobParticipantId);

        GameSession s = gameSessionRepository.findById(sessionId).orElseThrow();
        assertThat(s.getPhase()).isEqualTo(GamePhase.FIRST_HALF);
        assertThat(s.getRollLimit()).isEqualTo(0);
        assertThat(s.getParticipants()).allMatch(p -> p.getLastRoll() == null);

        // ── ROUND 1: starter rolls and finishes turn ──
        UUID starterId = s.getParticipants().get(s.getActiveParticipantIndex()).getId();
        gameService.performVirtualRoll(sessionId, starterId);

        s = gameSessionRepository.findById(sessionId).orElseThrow();
        assertThat(participantById(s, starterId).getThrowCount())
                .as("Starter must have throwCount = 1 after one roll")
                .isEqualTo(1);

        gameService.finishTurn(sessionId, starterId);

        s = gameSessionRepository.findById(sessionId).orElseThrow();
        assertThat(s.getRollLimit())
                .as("rollLimit must be fixed to starter's throwCount after finishTurn")
                .isEqualTo(1);

        // ── Second player rolls ──
        UUID secondId = s.getParticipants().get(s.getActiveParticipantIndex()).getId();
        gameService.performVirtualRoll(sessionId, secondId);

        // Capture stack size as a primitive BEFORE evaluation.
        // NOTE: within @Transactional, gameSessionRepository.findById() returns the same
        // JPA first-level-cache object every time — re-fetching does NOT give a true "before"
        // snapshot of the entity. A primitive int IS safely copied by value, so stackBeforeEval
        // is immune to this. The loser's exact pre-eval chip count cannot be snapshot-ed the
        // same way (we don't know who the loser is until after evaluation), so the deterministic
        // chip-delta assertion lives in Checkpoint 3, which sets rolls explicitly.
        int stackBeforeEval = gameSessionRepository.findById(sessionId).orElseThrow().getCentralStack();

        // ── Evaluate round ──
        RoundResultDTO result = gameService.evaluateRoundAndDistributeChips(
                sessionId, List.of(starterId, secondId));

        s = gameSessionRepository.findById(sessionId).orElseThrow();
        int chipsTransferred = result.chipsTransferred();

        // ✅ Checkpoint 5a: at least 1 chip must be transferred per round
        assertThat(chipsTransferred)
                .as("At least 1 chip must be transferred per round")
                .isGreaterThan(0);

        // ✅ Checkpoint 5b: stack decreased (chips came from somewhere real)
        assertThat(s.getCentralStack())
                .as("Central stack must not increase after a round")
                .isLessThanOrEqualTo(stackBeforeEval);

        // ✅ Checkpoint 5c: all rolls reset for next round
        assertThat(s.getParticipants()).allMatch(p -> p.getLastRoll() == null);
        assertThat(s.getRollLimit()).isEqualTo(0);

        // ✅ Checkpoint 5d: game still running after one round
        assertThat(s.getPhase()).isIn(GamePhase.FIRST_HALF, GamePhase.SECOND_HALF);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Finds a participant by their GameParticipant UUID. */
    private GameParticipant participantById(GameSession session, UUID participantId) {
        return session.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Participant not found by ID: " + participantId));
    }

    /** Finds a participant by the associated Player's name — used only in setUp(). */
    private GameParticipant participantByName(GameSession session, String playerName) {
        return session.getParticipants().stream()
                .filter(p -> p.getPlayer().getName().equals(playerName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Participant not found by player name: " + playerName));
    }
}
