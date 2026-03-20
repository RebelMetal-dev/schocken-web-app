package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.GameParticipant;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates the outcome of a Schocken round.
 * Determines which participant loses based on their DiceRoll.
 */
@Component
public class RoundEvaluator {

    /**
     * Finds the losing participant in a round.
     *
     * Comparison priority:
     *   1. RollType rank     (lower rank = worse)
     *   2. Hand status       (non-hand = worse)
     *   3. Throw count       (more throws = worse)
     *   4. Dice value        (lower value = worse)
     *
     * Tie-breaker ("Mitkommen ist verloren"): on a perfect tie,
     * the participant who rolled LATER in the sequence loses.
     *
     * @param participants Ordered list (index 0 = rolled first).
     * @return The losing GameParticipant.
     * @throws IllegalArgumentException if the list is empty or any participant has no roll.
     */
    public GameParticipant findLoser(List<GameParticipant> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("Participant list must not be empty.");
        }

        GameParticipant loser = null;
        for (GameParticipant current : participants) {
            if (current.getLastRoll() == null) {
                throw new IllegalArgumentException(
                    "Participant " + current.getPlayer().getName() + " has no roll.");
            }
            if (loser == null) {
                loser = current;
                continue;
            }
            // <= 0: current is equally bad or worse → later roller takes the loss
            if (current.getLastRoll().compareTo(loser.getLastRoll()) <= 0) {
                loser = current;
            }
        }
        return loser;
    }
}
