package de.rebelmetal.schockenwebapp.service;


import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service class for the business logic of player management.
 * Decouples database access (repository) from the web layer (controller).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;

    private final DiceService diceService;

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Transactional
    public Player createPlayer(String name) {
        Player player = new Player(UUID.randomUUID(), name);
        Player savedPlayer = playerRepository.save(player);
        log.info("New player created: {} (ID: {})", savedPlayer.getName(), savedPlayer.getId());
        return savedPlayer;
    }

    @Transactional
    public void deletePlayer(UUID id) {
        playerRepository.deleteById(id);
        log.info("Player with ID {} deleted.", id);
    }
/*
    *//**
     * Raise the amount of penalty-chips for the player.
     * Validation of game rules (e.g., maximum of 13 penalty chips) could be performed here.
     *//*
    @Transactional
    public Player addPenaltyChips(UUID id, int amount) {
        return playerRepository.findById(id).map(p -> {
            p.setPenaltyChips(p.getPenaltyChips() + amount);
            Player savedPlayer = playerRepository.save(p);
            log.info("Added {} penalty chips to player '{}'. Total: {}", amount, savedPlayer.getName(), savedPlayer.getPenaltyChips());
            return savedPlayer;
        }).orElseThrow(() -> new PlayerNotFoundException("Player with ID " + id + " not found"));
    }

    @Transactional
    public DiceRoll performVirtualRoll(UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player with ID " + playerId + " not found"));

        DiceRoll roll = diceService.rollVirtually();

        player.setLastRoll(roll); // Assign the roll to the player
        playerRepository.save(player); // Save the updated player state

        log.info("Player '{}' performed virtual roll: {}", player.getName(), roll);
        return roll;
    }

    @Transactional
    public DiceRoll performManualRoll(UUID playerId, int d1, int d2, int d3) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player with ID " + playerId + " not found"));

        DiceRoll roll = diceService.rollManually(d1, d2, d3);

        player.setLastRoll(roll); // Assign the manual roll to the player
        playerRepository.save(player); // Save the updated player state

        log.info("Player '{}' performed manual roll: {}", player.getName(), roll);
        return roll;
    }

    @Transactional
    public void resetAllDice() {
        // 1. Fetch all players from DB
        List<Player> allPlayers = playerRepository.findAll();

        // 2. Clear the last roll for every player
        allPlayers.forEach(player -> player.setLastRoll(null));

        // 3. Save all updated players at once
        playerRepository.saveAll(allPlayers);

        log.info("Round ended: All dice have been cleared from the table for {} players.", allPlayers.size());
    }*/
}
