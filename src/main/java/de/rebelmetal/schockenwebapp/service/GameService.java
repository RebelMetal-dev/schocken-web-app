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

        List<Player> players = playerIds.stream()
                .map(id -> playerRepository.findById(id)
                        .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + id)))
                .toList();

        // new ArrayList<>() is required here — Stream.toList() returns an unmodifiable list.
        // With @Version on GameSession, Hibernate's merge path calls replaceElements() on
        // the participants collection, which calls clear() internally. An unmodifiable list
        // throws UnsupportedOperationException at that point.
        List<GameParticipant> participants = new ArrayList<>(players.stream().map(player -> {
            GameParticipant p = new GameParticipant();
            p.setId(UUID.randomUUID());
            p.setPlayer(player);
            p.setSession(session);
            p.setPenaltyChips(0);
            return p;
        }).toList());

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
        boolean isHand = (p.getThrowCount() == 0);
        DiceRoll roll = diceService.rollVirtually(isHand, p.getThrowCount() + 1);
        return applyRoll(session, p, roll);
    }

    @Transactional
    public RoundResultDTO evaluateRoundAndDistributeChips(UUID sessionId, List<UUID> participantIds) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        if (session.getPhase() == GamePhase.GAME_OVER) {
            throw new IllegalStateException("Cannot evaluate: game is already over.");
        }

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

        if (session.getPhase() == GamePhase.FINAL_MATCH) {
            // 4d — Final match: exactly 2 finalists, loser of the round ends the game
            if (rollers.size() != 2) {
                throw new IllegalStateException(
                        "FINAL_MATCH requires exactly 2 rollers, got: " + rollers.size());
            }
            if (winner.getLastRoll().isShockOut()) {
                handleShockOut(session, loser);
            } else {
                distributeChips(session, winner, loser, penalty);
            }
            if (session.getCentralStack() == 0) {
                log.info("Final match over. {} lost the game.", loser.getPlayer().getName());
                session.setPhase(GamePhase.GAME_OVER);
            }
        } else {
            if (winner.getLastRoll().isShockOut()) {
                handleShockOut(session, loser);
            } else {
                distributeChips(session, winner, loser, penalty);
            }
            handlePhaseTransitions(session, loser);
        }

        // Showdown reveal: set all cups visible so the frontend can display results.
        // Must happen BEFORE resetRoundState(), which clears cupRevealed for the next round.
        rollers.forEach(p -> p.setCupRevealed(true));

        // Pitter rule: the round loser becomes the Beginner (starter) of the next round.
        // activeParticipantIndex is set directly to the loser's position — not incremented via modulo.
        // Skipped when the game is over — no next round to prepare (SCHOCKEN_RULES.md §2c).
        boolean gameOver = session.getPhase() == GamePhase.GAME_OVER;
        if (!gameOver) {
            session.setActiveParticipantIndex(session.getParticipants().indexOf(loser));
        }

        // Hard reset: clear all per-round state so the next round starts clean.
        resetRoundState(session, rollers);

        gameSessionRepository.save(session);
        return new RoundResultDTO(
                loser.getId(),
                loser.getPlayer().getName(),
                penalty,
                rollsSnapshot,
                gameOver
        );
    }

    // 4d — Returns the two finalists in participant list order (first-half loser first if applicable).
    // Only valid once FINAL_MATCH or GAME_OVER has been reached.
    public List<GameParticipant> getOrderedFinalists(GameSession session) {
        if (session.getPhase() != GamePhase.FINAL_MATCH
                && session.getPhase() != GamePhase.GAME_OVER) {
            throw new IllegalStateException(
                    "getOrderedFinalists requires phase FINAL_MATCH or GAME_OVER, but was: "
                    + session.getPhase());
        }
        return session.getParticipants().stream()
                .filter(p -> p.isLostFirstHalf() || p.isLostSecondHalf())
                .toList();
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

    // 4c — Phase transition logic after each chip distribution.
    // Two if-blocks (not else-if): a ShockOut on a full stack could trigger both
    // transitions in a single call — the centralStack reset to 13 prevents double-firing.
    private void handlePhaseTransitions(GameSession session, GameParticipant loser) {
        if (session.getPhase() == GamePhase.FIRST_HALF && session.getCentralStack() == 0) {
            loser.setLostFirstHalf(true);
            log.info("First half over. {} lost.", loser.getPlayer().getName());
            session.getParticipants().forEach(p -> p.setPenaltyChips(0));
            session.setCentralStack(13);
            session.setPhase(GamePhase.SECOND_HALF);
        }

        if (session.getPhase() == GamePhase.SECOND_HALF && session.getCentralStack() == 0) {
            loser.setLostSecondHalf(true);
            log.info("Second half over. {} lost.", loser.getPlayer().getName());
            if (loser.hasLostMatch()) {
                // Same player lost both halves — no final needed
                session.setPhase(GamePhase.GAME_OVER);
            } else {
                // Two different losers — they play a final match
                session.setPhase(GamePhase.FINAL_MATCH);
            }
        }
    }

    private void handleShockOut(GameSession session, GameParticipant loser) {
        loser.setPenaltyChips(loser.getPenaltyChips() + session.getCentralStack());
        session.setCentralStack(0);
    }

    // Clears all per-round state to prepare for the next round.
    // Participant state (lastRoll, throwCount, cupRevealed) is reset via resetRoll().
    // Session rollLimit is cleared so the next Beginner (starter) sets a new limit.
    private void resetRoundState(GameSession session, List<GameParticipant> participants) {
        participants.forEach(GameParticipant::resetRoll);
        session.setRollLimit(0);
    }

    @Transactional(readOnly = true)
    public GameSession getSession(UUID sessionId) {
        return gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
    }

    // --- Turn Management ---

    // Central validation hub for all roll actions (virtual and manual).
    // Enforces turn order and roll limit before any dice state is written.
    // Called exclusively from performVirtualRoll() and performManualRoll() — never from outside.
    private GameParticipant applyRoll(GameSession session, GameParticipant participant, DiceRoll roll) {
        // 1. Turn check: only the active player may roll
        GameParticipant active = session.getParticipants().get(session.getActiveParticipantIndex());
        if (!active.getId().equals(participant.getId())) {
            throw new IllegalStateException(
                    "It is not " + participant.getPlayer().getName() + "'s turn. " +
                    "Active player: " + active.getPlayer().getName());
        }

        // 2. Limit guard (SCHOCKEN_RULES.md §2a).
        // rollLimit == 0 means the Beginner has not finished their turn yet (setup/initial roll phase).
        // In that state no limit is enforced, so the Beginner may roll up to 3 times freely.
        if (session.getRollLimit() > 0 && participant.getThrowCount() >= session.getRollLimit()) {
            throw new IllegalStateException(
                    "Roll limit of " + session.getRollLimit() + " reached for "
                    + participant.getPlayer().getName());
        }

        // 3. Apply roll
        participant.setLastRoll(roll);
        participant.setThrowCount(participant.getThrowCount() + 1);
        return gameParticipantRepository.save(participant);
    }

    // Called when the active player voluntarily stops rolling (or has no rolls left).
    // Fixates rollLimit on the Beginner's first finishTurn, then advances the turn pointer.
    @Transactional
    public GameSession finishTurn(UUID sessionId, UUID participantId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        GameParticipant p = session.getParticipants().stream()
                .filter(part -> part.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Participant " + participantId + " not in session " + sessionId));

        // Turn check: only the active player may end their turn
        GameParticipant active = session.getParticipants().get(session.getActiveParticipantIndex());
        if (!active.getId().equals(p.getId())) {
            throw new IllegalStateException(
                    "It is not " + p.getPlayer().getName() + "'s turn to finish.");
        }

        // Fixate rollLimit: Beginner's throwCount becomes the limit for all other players.
        // Causally must happen before the index is advanced (SCHOCKEN_RULES.md §2a).
        if (session.getRollLimit() == 0) {
            session.setRollLimit(p.getThrowCount());
            log.info("Roll limit set to {} by Beginner {}.", p.getThrowCount(), p.getPlayer().getName());
        }

        // Advance turn pointer using modulo to wrap around the participant list
        int nextIndex = (session.getActiveParticipantIndex() + 1) % session.getParticipants().size();
        session.setActiveParticipantIndex(nextIndex);

        return gameSessionRepository.save(session);
    }

    // Called when a player clicks the "Lupfen" (reveal cup) button (SCHOCKEN_RULES.md §3.2).
    // Idempotent: revealing an already-revealed cup is a no-op.
    // Blind-Zwang violation: if the player reached the roll limit and reveals anyway,
    // they receive 1 penalty chip immediately and the cup stays revealed.
    @Transactional
    public GameParticipant revealCup(UUID sessionId, UUID participantId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        GameParticipant p = session.getParticipants().stream()
                .filter(part -> part.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Participant " + participantId + " not in session " + sessionId));

        if (p.isCupRevealed()) {
            return p;
        }

        // Blind-Zwang: player who exhausted the roll limit must keep cup covered (§2a).
        // blindMandatory is computed state — no extra field needed.
        boolean blindMandatory = session.getRollLimit() > 0
                && p.getThrowCount() >= session.getRollLimit();
        if (blindMandatory) {
            int penalty = Math.min(session.getCentralStack(), 1);
            session.setCentralStack(session.getCentralStack() - penalty);
            p.setPenaltyChips(p.getPenaltyChips() + penalty);
            log.warn("Blind-Zwang violated by {}. 1 penalty chip applied.", p.getPlayer().getName());
            gameSessionRepository.save(session);
        }

        p.setCupRevealed(true);
        return gameParticipantRepository.save(p);
    }

    @Transactional
    public GameParticipant performManualRoll(UUID sessionId, UUID participantId, int d1, int d2, int d3, boolean hand, int count) {
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();
        GameParticipant p = session.getParticipants().stream()
                .filter(part -> part.getId().equals(participantId))
                .findFirst()
                .orElseThrow();
        return applyRoll(session, p, new DiceRoll(d1, d2, d3, hand, count));
    }
}