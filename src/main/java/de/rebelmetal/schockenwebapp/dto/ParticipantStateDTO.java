package de.rebelmetal.schockenwebapp.dto;

import de.rebelmetal.schockenwebapp.model.GameParticipant;

import java.util.List;
import java.util.UUID;

/**
 * Filtered projection of a single participant's state.
 * Dice values are only included when the cup has been physically revealed.
 * A null diceValues field signals to the client that the cup is still covered.
 */
public record ParticipantStateDTO(
        UUID participantId,
        String playerName,
        int penaltyChips,
        int throwCount,
        boolean cupRevealed,
        List<Integer> diceValues   // null = cup is covered; [x,y,z] = visible
) {
    /**
     * Maps a GameParticipant to its filtered state projection.
     * Dice values are only exposed when cupRevealed == true and a roll exists.
     */
    public static ParticipantStateDTO from(GameParticipant p) {
        List<Integer> dice = (p.isCupRevealed() && p.getLastRoll() != null)
                ? p.getLastRoll().getDice()
                : null;
        return new ParticipantStateDTO(
                p.getId(),
                p.getPlayer().getName(),
                p.getPenaltyChips(),
                p.getThrowCount(),
                p.isCupRevealed(),
                dice
        );
    }
}
