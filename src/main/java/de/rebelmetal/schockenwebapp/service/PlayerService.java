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
}
