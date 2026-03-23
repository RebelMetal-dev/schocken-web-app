package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.GameParticipant;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates the outcome of a Schocken round.
 * Determines which participant loses and which wins based on their DiceRoll.
 *
 * List order is significant: index 0 = rolled first.
 * LIFO tie-breaker for loser: later roller loses on a perfect tie.
 * FIFO tie-breaker for winner: earlier roller wins on a perfect tie.
 */
@Component
public class RoundEvaluator {

    /**
     * Finds the losing participant in a round.
     * Tie-breaker (LIFO): on a perfect tie, the participant who rolled LATER loses.
     *
     * @param participants Ordered list (index 0 = rolled first).
     * @return The losing GameParticipant.
     * @throws IllegalArgumentException if the list is empty or any participant has no roll.
     */
    public GameParticipant findLoser(List<GameParticipant> participants) {
        validateParticipants(participants);

        GameParticipant loser = null;
        for (GameParticipant current : participants) {
            validateRoll(current);
            if (loser == null) {
                loser = current;
                continue;
            }
            // <= 0: current is equally bad or worse → later roller takes the loss (LIFO)
            if (current.getLastRoll().compareTo(loser.getLastRoll()) <= 0) {
                loser = current;
            }
        }
        return loser;
    }

    /**
     * Finds the winning participant in a round.
     * Tie-breaker (FIFO): on a perfect tie, the participant who rolled EARLIER wins.
     *
     * @param participants Ordered list (index 0 = rolled first).
     * @return The winning GameParticipant.
     * @throws IllegalArgumentException if the list is empty or any participant has no roll.
     */
    public GameParticipant findWinner(List<GameParticipant> participants) {
        validateParticipants(participants);

        GameParticipant winner = null;
        for (GameParticipant current : participants) {
            validateRoll(current);
            if (winner == null) {
                winner = current;
                continue;
            }
            // > 0 strictly: current must be better to displace the current winner (FIFO)
            if (current.getLastRoll().compareTo(winner.getLastRoll()) > 0) {
                winner = current;
            }
        }
        return winner;
    }

    private void validateParticipants(List<GameParticipant> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("Participant list must not be empty.");
        }
    }

    private void validateRoll(GameParticipant participant) {
        if (participant.getLastRoll() == null) {
            throw new IllegalArgumentException(
                "Participant " + participant.getPlayer().getName() + " has no roll.");
        }
    }
}
