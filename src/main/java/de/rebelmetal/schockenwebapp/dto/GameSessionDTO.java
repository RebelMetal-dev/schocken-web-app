package de.rebelmetal.schockenwebapp.dto;

import de.rebelmetal.schockenwebapp.model.GamePhase;

import java.util.List;
import java.util.UUID;

public record GameSessionDTO(
        UUID id,
        GamePhase phase,
        int centralStack,
        List<ParticipantDTO> participants
) {}
