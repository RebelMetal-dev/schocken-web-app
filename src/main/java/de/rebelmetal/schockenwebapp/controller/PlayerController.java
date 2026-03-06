package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für die Spieler-API.
 * Delegiert alle fachlichen Aufgaben an den PlayerService.
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @PostMapping
    public Player addPlayer(@RequestBody String name) {
        return playerService.createPlayer(name);
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable UUID id) {
        playerService.deletePlayer(id);
    }

    @PutMapping("/{id}/deckel")
    public Player updateDeckel(@PathVariable UUID id, @RequestBody int anzahl) {
        return playerService.addDeckel(id, anzahl);
    }
}