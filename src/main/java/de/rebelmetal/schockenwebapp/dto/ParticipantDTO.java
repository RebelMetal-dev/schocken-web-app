package de.rebelmetal.schockenwebapp.dto;

import de.rebelmetal.schockenwebapp.model.DiceRoll;

import java.util.UUID;

public record ParticipantDTO(
        UUID id,
        String playerName,
        int penaltyChips,
        boolean lostFirstHalf,
        boolean lostSecondHalf,
        DiceRoll lastRoll,
        int throwCount,
        boolean safe
) {}
