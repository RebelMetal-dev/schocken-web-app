package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.DiceRoll;
import de.rebelmetal.schockenwebapp.model.GameParticipant;
import de.rebelmetal.schockenwebapp.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoundEvaluatorTest {

    private RoundEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new RoundEvaluator();
    }

    private GameParticipant participantWithRoll(String name, DiceRoll roll) {
        Player player = new Player(UUID.randomUUID(), name);
        GameParticipant p = new GameParticipant();
        p.setId(UUID.randomUUID());
        p.setPlayer(player);
        p.setLastRoll(roll);
        return p;
    }

    @Test
    void shouldWinWithHandOverCombined() {
        // Shock 2: dice (2,1,1). Hand in 1 throw vs combined in 2 throws — same rank, hand wins.
        GameParticipant alice = participantWithRoll("Alice",
                new DiceRoll(2, 1, 1, true, 1));   // Shock 2, hand, 1 throw
        GameParticipant bob = participantWithRoll("Bob",
                new DiceRoll(2, 1, 1, false, 2));   // Shock 2, combined, 2 throws

        GameParticipant loser = evaluator.findLoser(List.of(alice, bob));

        assertEquals("Bob", loser.getPlayer().getName(),
                "Bob (combined) must lose against Alice (hand) — hand bonus applies.");
    }

    @Test
    void shouldApplyLIFOWhoseComingIsLost() {
        // Both Shock 3, both hand, same throwCount — perfect tie → later roller loses.
        GameParticipant first = participantWithRoll("First",
                new DiceRoll(3, 1, 1, true, 1));
        GameParticipant second = participantWithRoll("Second",
                new DiceRoll(3, 1, 1, true, 1));

        GameParticipant loser = evaluator.findLoser(List.of(first, second));

        assertEquals("Second", loser.getPlayer().getName(),
                "Second (later roller) must lose on a perfect tie — Mitkommen ist verloren.");
    }

    @Test
    void shouldThrowExceptionForInvalidDice() {
        assertThrows(IllegalArgumentException.class,
                () -> new DiceRoll(7, 1, 1),
                "Dice value 7 is out of range and must throw IllegalArgumentException.");
    }
}
