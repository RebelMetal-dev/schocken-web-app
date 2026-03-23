package de.rebelmetal.schockenwebapp.dto;

import de.rebelmetal.schockenwebapp.model.DiceRoll;

import java.util.Map;
import java.util.UUID;

/**
 * Immutable snapshot of a round result.
 * Captured before the database state is reset for the next round.
 */
public record RoundResultDTO(
        UUID loserParticipantId,
        String loserName,
        int chipsTransferred,
        Map<UUID, DiceRoll> allRolls,
        boolean isGameOver
) {}
