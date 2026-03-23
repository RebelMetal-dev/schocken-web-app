package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.dto.RoundResultDTO;
import de.rebelmetal.schockenwebapp.exception.PlayerNotFoundException;
import de.rebelmetal.schockenwebapp.model.*;
import de.rebelmetal.schockenwebapp.repository.GameParticipantRepository;
import de.rebelmetal.schockenwebapp.repository.GameSessionRepository;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final GameParticipantRepository gameParticipantRepository;
    private final PlayerRepository playerRepository;
    private final DiceService diceService;
    private final RoundEvaluator roundEvaluator;

    @Transactional
    public GameSession createSession(List<UUID> playerIds) {
        GameSession session = new GameSession();
        session.setId(UUID.randomUUID());
        session.setPhase(GamePhase.WAITING_FOR_PLAYERS);
        session.setCentralStack(13);

        List<Player> players = playerRepository.findAllById(playerIds);
        if (players.size() != playerIds.size()) throw new PlayerNotFoundException("Some players not found.");

        List<GameParticipant> participants = players.stream().map(player -> {
            GameParticipant p = new GameParticipant();
            p.setId(UUID.randomUUID());
            p.setPlayer(player);
            p.setSession(session);
            p.setPenaltyChips(0);
            return p;
        }).toList();

        session.setParticipants(participants);
        return gameSessionRepository.save(session);
    }

    @Transactional
    public List<GameParticipant> evaluateSetupAndDetermineOrder(UUID sessionId, List<UUID> participantIds) {
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();

        if (session.getPhase() != GamePhase.WAITING_FOR_PLAYERS
                && session.getPhase() != GamePhase.SETTING_UP_ORDER) {
            throw new IllegalStateException(
                    "evaluateSetupAndDetermineOrder requires phase WAITING_FOR_PLAYERS or SETTING_UP_ORDER, " +
                    "but was: " + session.getPhase());
        }

        List<GameParticipant> setupRollers = participantIds.stream()
                .map(id -> session.getParticipants().stream().filter(p -> p.getId().equals(id)).findFirst().orElseThrow())
                .toList();

        List<GameParticipant> lowestRollers = roundEvaluator.findAllLowestRollers(setupRollers);

        if (lowestRollers.size() == 1) {
            // Only one loser -> Setup finished
            GameParticipant loser = lowestRollers.get(0);
            reorderParticipantsStartingWith(session, loser);
            session.setPhase(GamePhase.FIRST_HALF);
            log.info("Setup complete. Loser: {}", loser.getPlayer().getName());
        } else {
            // Tie-break (Stechen) -> transition to SETTING_UP_ORDER, clear rolls for tied participants
            session.setPhase(GamePhase.SETTING_UP_ORDER);
            log.info("Setup tie-break needed for {} players.", lowestRollers.size());
            lowestRollers.forEach(p -> p.setLastRoll(null));
        }

        gameSessionRepository.save(session);
        return lowestRollers;
    }

    private void reorderParticipantsStartingWith(GameSession session, GameParticipant loser) {
        List<GameParticipant> current = new ArrayList<>(session.getParticipants());
        int loserIndex = current.indexOf(loser);
        if (loserIndex == -1) return;

        List<GameParticipant> newOrder = new ArrayList<>();
        int size = current.size();
        for (int i = 0; i < size; i++) {
            newOrder.add(current.get((loserIndex + i) % size));
        }
        session.setParticipants(newOrder);
    }

    @Transactional
    public GameParticipant performVirtualRoll(UUID sessionId, UUID participantId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        GameParticipant p = session.getParticipants().stream()
                .filter(part -> part.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Participant " + participantId + " not in session " + sessionId));
        int newThrowCount = p.getThrowCount() + 1;
        boolean isHand = (newThrowCount == 1);
        DiceRoll roll = diceService.rollVirtually(isHand, newThrowCount);
        p.setLastRoll(roll);
        p.setThrowCount(newThrowCount);
        return gameParticipantRepository.save(p);
    }

    @Transactional
    public RoundResultDTO evaluateRoundAndDistributeChips(UUID sessionId, List<UUID> participantIds) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        // map() preserves input order — critical for LIFO/FIFO tie-breaking
        List<GameParticipant> rollers = participantIds.stream()
                .map(id -> session.getParticipants().stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("Participant not in session: " + id)))
                .toList();

        // 4a.1 — Null-Guard: all participants must have rolled before evaluation
        if (rollers.stream().anyMatch(p -> p.getLastRoll() == null)) {
            throw new IllegalStateException(
                    "Cannot evaluate round: not all participants have rolled yet.");
        }

        GameParticipant loser  = roundEvaluator.findLoser(rollers);
        GameParticipant winner = roundEvaluator.findWinner(rollers);
        int penalty = roundEvaluator.calculatePenalty(winner.getLastRoll());

        // 4b — Snapshot BEFORE reset: frontend sees what everyone rolled
        Map<UUID, DiceRoll> rollsSnapshot = rollers.stream()
                .collect(Collectors.toMap(GameParticipant::getId, GameParticipant::getLastRoll));

        if (winner.getLastRoll().isShockOut()) {
            handleShockOut(session, loser);
        } else {
            distributeChips(session, winner, loser, penalty);
        }

        if (session.getCentralStack() == 0) session.setPhase(GamePhase.SECOND_HALF);

        // 4b — Auto-reset all rolls for the next round
        rollers.forEach(GameParticipant::resetRoll);

        gameSessionRepository.save(session);
        return new RoundResultDTO(
                loser.getId(),
                loser.getPlayer().getName(),
                penalty,
                rollsSnapshot,
                session.getPhase() == GamePhase.GAME_OVER
        );
    }

    private void distributeChips(GameSession session, GameParticipant winner, GameParticipant loser, int amount) {
        int fromStack = Math.min(session.getCentralStack(), amount);
        session.setCentralStack(session.getCentralStack() - fromStack);
        loser.setPenaltyChips(loser.getPenaltyChips() + fromStack);

        int remaining = amount - fromStack;
        if (remaining > 0 && session.getCentralStack() == 0) {
            int stolen = Math.min(winner.getPenaltyChips(), remaining);
            winner.setPenaltyChips(winner.getPenaltyChips() - stolen);
            loser.setPenaltyChips(loser.getPenaltyChips() + stolen);
            if (stolen < remaining) {
                log.warn("Chip deficit: {} chip(s) could not be distributed. " +
                         "Stack empty, winner '{}' has 0 chips. Total in play may be < 13.",
                        remaining - stolen, winner.getPlayer().getName());
            }
        }
    }

    private void handleShockOut(GameSession session, GameParticipant loser) {
        loser.setPenaltyChips(loser.getPenaltyChips() + session.getCentralStack());
        session.setCentralStack(0);
    }

    @Transactional
    public GameParticipant performManualRoll(UUID sessionId, UUID participantId, int d1, int d2, int d3, boolean hand, int count) {
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();
        GameParticipant p = session.getParticipants().stream().filter(part -> part.getId().equals(participantId)).findFirst().orElseThrow();
        p.setLastRoll(new DiceRoll(d1, d2, d3, hand, count));
        p.setThrowCount(count);
        return gameParticipantRepository.save(p);
    }
}