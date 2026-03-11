package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.model.DiceRoll;
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
    public Player createPlayer(@RequestBody Player player) {
        return playerService.createPlayer(player.getName());
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable UUID id) {
        playerService.deletePlayer(id);
    }

    @PutMapping("/{id}/deckel")
    public Player updateDeckel(@PathVariable UUID id, @RequestBody int anzahl) {
        return playerService.addDeckel(id, anzahl);
    }

    @PostMapping("/{id}/roll-virtual")
    public DiceRoll rollVirtual(@PathVariable UUID id) {
        // Hier rufen wir den Chef-Service auf
        return playerService.performVirtualRoll(id);
    }

    @PostMapping("/{id}/roll-manual")
    public DiceRoll rollManual(
            @PathVariable UUID id,
            @RequestParam int d1,
            @RequestParam int d2,
            @RequestParam int d3) {
        // Hier leiten wir die Handeingabe weiter
        return playerService.performManualRoll(id, d1, d2, d3);
    }
}