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

    /**
     * A Hand-Roll (hand=true, throwCount=2) must beat a Combined-Roll
     * (hand=false, throwCount=2) of the same dice values.
     * The combined-roll player loses.
     */
    @Test
    void handRollBeatsNonHandRollOfSameValue() {
        GameParticipant alice = participantWithRoll("Alice",
                new DiceRoll(4, 3, 2, true, 2));   // hand
        GameParticipant bob = participantWithRoll("Bob",
                new DiceRoll(4, 3, 2, false, 2));   // combined

        GameParticipant loser = evaluator.findLoser(List.of(alice, bob));

        assertEquals("Bob", loser.getPlayer().getName(),
                "Bob (non-hand) should lose against Alice (hand) with identical dice.");
    }

    /**
     * "Mitkommen ist verloren": if two participants have identical rolls
     * in every dimension, the one who rolled LATER loses.
     */
    @Test
    void lifoTieBreakerAppliesOnPerfectTie() {
        GameParticipant first = participantWithRoll("First",
                new DiceRoll(3, 2, 1, false, 2));
        GameParticipant second = participantWithRoll("Second",
                new DiceRoll(3, 2, 1, false, 2));

        // first rolled at index 0, second at index 1
        GameParticipant loser = evaluator.findLoser(List.of(first, second));

        assertEquals("Second", loser.getPlayer().getName(),
                "Second (later roller) must lose on a perfect tie — Mitkommen ist verloren.");
    }
}
