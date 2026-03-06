package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.model.Player;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final List<Player> players = new ArrayList<>();

    @GetMapping
    public List<Player> getAllPlayers() {
        return players;
    }

    @PostMapping
    public Player addPlayer(@RequestBody String name) {
        Player newPlayer = new Player(UUID.randomUUID(), name, 0, false);
        players.add(newPlayer);
        return newPlayer;
    }

    @DeleteMapping("/{id}")
    public List<Player> deletePlayer(@PathVariable UUID id) {
        players.removeIf(p -> p.getId().equals(id));
        return players;
    }

    @PutMapping("/{id}/deckel")
    public Player updateDeckel(@PathVariable UUID id, @RequestBody int neueAnzahl) {
        return players.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(p -> {
                    p.setDeckel(neueAnzahl);
                    return p;
                })
                .orElse(null);
    }

    @PostMapping("/reset")

    public List<Player> resetGame() {
        players.forEach(p -> {
            p.setDeckel(0);
            p.setIstSicher(false);
        });

        return players;
    }


}
