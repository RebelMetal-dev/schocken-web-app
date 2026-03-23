package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.*;
import de.rebelmetal.schockenwebapp.repository.GameSessionRepository;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private GameSession testSession;
    private GameParticipant player1;
    private GameParticipant player2;
    private GameParticipant player3;

    @BeforeEach
    void setUp() {
        // Create 3 persistent players
        Player p1 = playerRepository.save(new Player("Alice"));
        Player p2 = playerRepository.save(new Player("Bob"));
        Player p3 = playerRepository.save(new Player("Charlie"));

        // Initialize a new game session via service
        testSession = gameService.createSession(List.of(p1.getId(), p2.getId(), p3.getId()));

        // Map participants for easy access in tests
        player1 = testSession.getParticipants().get(0);
        player2 = testSession.getParticipants().get(1);
        player3 = testSession.getParticipants().get(2);
    }

    @Test
    void evaluateSetupAndDetermineOrder_withTieBreak_transitionsToSettingUpOrderAndClearsRolls() {
        // Given: player1 and player2 have the WORST (identical) roll → Stechen (tie-break)
        //        player3 has the best roll → not involved in the tie-break
        player1.setLastRoll(new DiceRoll(6, 5, 3, false, 1)); // HOUSE_NUMBER — worst (6=5+1, but 5≠3+1)
        player2.setLastRoll(new DiceRoll(6, 5, 3, false, 1)); // HOUSE_NUMBER — equally worst (Tie!)
        player3.setLastRoll(new DiceRoll(4, 4, 4, false, 1)); // TRIPLET      — best, not in tie-break

        gameSessionRepository.save(testSession);

        // When: participantIds define the play order (required for LIFO/FIFO tie-breaking)
        gameService.evaluateSetupAndDetermineOrder(
                testSession.getId(),
                List.of(player1.getId(), player2.getId(), player3.getId())
        );

        // Then: Stechen → phase stays SETTING_UP_ORDER
        GameSession updatedSession = gameSessionRepository.findById(testSession.getId()).orElseThrow();
        assertThat(updatedSession.getPhase()).isEqualTo(GamePhase.SETTING_UP_ORDER);

        // Only the tied losers (player1, player2) have their rolls cleared
        List<UUID> tiedIds = List.of(player1.getId(), player2.getId());
        assertThat(updatedSession.getParticipants())
                .filteredOn(p -> tiedIds.contains(p.getId()))
                .allMatch(p -> p.getLastRoll() == null);

        // player3 (clear winner) keeps their roll
        assertThat(updatedSession.getParticipants())
                .filteredOn(p -> p.getId().equals(player3.getId()))
                .allMatch(p -> p.getLastRoll() != null);
    }

    @Test
    void evaluateRound_withShockOut_givesRemainingChipsToLoserAndTransitionsPhase() {
        // Given: Only 5 chips left in the central stack during the first half
        testSession.setCentralStack(5);
        testSession.setPhase(GamePhase.FIRST_HALF);

        // Player 1: Shock Out → Winner (highest possible roll)
        player1.setLastRoll(new DiceRoll(1, 1, 1, false, 1));
        // Player 2: House Number → Loser (6-5-3: 6=5+1, but 5≠3+1 → not a Straight)
        player2.setLastRoll(new DiceRoll(6, 5, 3, false, 1));
        // Player 3: Straight → Safe (3-4-5 is consecutive)
        player3.setLastRoll(new DiceRoll(3, 4, 5, false, 1));

        // Persist state before calling service logic
        gameSessionRepository.save(testSession);

        List<UUID> rollerIds = List.of(player1.getId(), player2.getId(), player3.getId());

        // When: Evaluating the round
        gameService.evaluateRoundAndDistributeChips(testSession.getId(), rollerIds);

        // Then: First half ends → chips reset to center, second half begins
        GameSession updatedSession = gameSessionRepository.findById(testSession.getId()).orElseThrow();
        GameParticipant updatedLoser = updatedSession.getParticipants().stream()
                .filter(p -> p.getId().equals(player2.getId()))
                .findFirst().orElseThrow();

        // 4c: lostFirstHalf flag set on the loser who triggered the half-end
        assertThat(updatedLoser.isLostFirstHalf()).isTrue();
        // 4c: all penalty chips returned to center (chip reset)
        assertThat(updatedSession.getParticipants()).allMatch(p -> p.getPenaltyChips() == 0);
        // 4c: central stack reset to 13 for the second half
        assertThat(updatedSession.getCentralStack()).isEqualTo(13);
        assertThat(updatedSession.getPhase()).isEqualTo(GamePhase.SECOND_HALF);
    }

    @Test
    void evaluateRound_withEmptyStack_redistributesChipsFromWinnerToLoser() {
        // Given: Stack is empty (Second Half), winner has chips to give away
        testSession.setCentralStack(0);
        testSession.setPhase(GamePhase.SECOND_HALF);

        // Winner starts with 4 chips and throws a Shock 2 (Value: 2 chips)
        player1.setPenaltyChips(4);
        player1.setLastRoll(new DiceRoll(1, 1, 2, false, 1));

        // Loser starts with 0 chips
        player2.setPenaltyChips(0);
        player2.setLastRoll(new DiceRoll(6, 5, 4, false, 1));

        // player3 must have a roll BETTER than player2's to ensure player2 remains the loser
        player3.setLastRoll(new DiceRoll(3, 3, 3, false, 1)); // TRIPLET — clearly beats HOUSE_NUMBER

        gameSessionRepository.save(testSession);

        List<UUID> rollerIds = List.of(player1.getId(), player2.getId(), player3.getId());

        // When: Evaluating the round
        gameService.evaluateRoundAndDistributeChips(testSession.getId(), rollerIds);

        // Then: 2 chips are moved from winner to loser
        GameSession updatedSession = gameSessionRepository.findById(testSession.getId()).orElseThrow();
        GameParticipant updatedWinner = updatedSession.getParticipants().stream()
                .filter(p -> p.getId().equals(player1.getId())).findFirst().orElseThrow();
        GameParticipant updatedLoser = updatedSession.getParticipants().stream()
                .filter(p -> p.getId().equals(player2.getId())).findFirst().orElseThrow();

        assertThat(updatedWinner.getPenaltyChips()).isEqualTo(2); // 4 - 2
        assertThat(updatedLoser.getPenaltyChips()).isEqualTo(2);  // 0 + 2
    }
}