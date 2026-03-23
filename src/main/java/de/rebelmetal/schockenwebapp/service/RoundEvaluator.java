package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.DiceRoll;
import de.rebelmetal.schockenwebapp.model.GameParticipant;
import de.rebelmetal.schockenwebapp.model.RollType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RoundEvaluator {

    public GameParticipant findLoser(List<GameParticipant> participants) {
        GameParticipant loser = null;
        for (GameParticipant current : participants) {
            // LIFO Tie-breaker: Later roller loses on identical roll (<= 0)
            if (loser == null || current.getLastRoll().compareTo(loser.getLastRoll()) <= 0) {
                loser = current;
            }
        }
        return loser;
    }

    public GameParticipant findWinner(List<GameParticipant> participants) {
        GameParticipant winner = null;
        for (GameParticipant current : participants) {
            // FIFO Tie-breaker: Earlier roller wins on identical roll (> 0)
            if (winner == null || current.getLastRoll().compareTo(winner.getLastRoll()) > 0) {
                winner = current;
            }
        }
        return winner;
    }

    public List<GameParticipant> findAllLowestRollers(List<GameParticipant> participants) {
        GameParticipant absoluteLoser = findLoser(participants);
        List<GameParticipant> lowestRollers = new ArrayList<>();
        for (GameParticipant p : participants) {
            if (p.getLastRoll().compareTo(absoluteLoser.getLastRoll()) == 0) {
                lowestRollers.add(p);
            }
        }
        return lowestRollers;
    }

    public int calculatePenalty(DiceRoll winnerRoll) {
        if (winnerRoll == null) return 0;
        return switch (winnerRoll.getType()) {
            case SHOCK_OUT -> 13;
            case SHOCK -> winnerRoll.getDice().get(0); // Value of the first die
            case TRIPLET -> 3;
            case STRAIGHT -> 2;
            case HOUSE_NUMBER -> 1;
        };
    }
}