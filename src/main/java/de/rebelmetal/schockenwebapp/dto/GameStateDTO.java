package de.rebelmetal.schockenwebapp.dto;

import de.rebelmetal.schockenwebapp.model.GamePhase;
import de.rebelmetal.schockenwebapp.model.GameSession;

import java.util.List;
import java.util.UUID;

/**
 * Read-only projection of a full game session's state.
 * Safe for broadcasting to all clients: dice values are filtered
 * per participant via ParticipantStateDTO (null when cup is covered).
 */
public record GameStateDTO(
        UUID sessionId,
        GamePhase phase,
        int centralStack,
        int rollLimit,
        int activeParticipantIndex,
        List<ParticipantStateDTO> participants
) {
    public static GameStateDTO from(GameSession session) {
        return new GameStateDTO(
                session.getId(),
                session.getPhase(),
                session.getCentralStack(),
                session.getRollLimit(),
                session.getActiveParticipantIndex(),
                session.getParticipants().stream()
                        .map(ParticipantStateDTO::from)
                        .toList()
        );
    }
}
