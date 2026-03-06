package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für die Verwaltung der Spieler.
 * Nutzt das PlayerRepository für die persistente Datenspeicherung in der H2-Datenbank.
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor // Erzeugt automatisch den Konstruktor für das Repository (Dependency Injection)
public class PlayerController {

    private final PlayerRepository playerRepository;

    /**
     * Ruft alle Spieler aus der Datenbank ab.
     */
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    /**
     * Speichert einen neuen Spieler in der Datenbank.
     */
    @PostMapping
    public Player addPlayer(@RequestBody String name) {
        Player newPlayer = new Player(UUID.randomUUID(), name, 0, false);
        return playerRepository.save(newPlayer);
    }

    /**
     * Löscht einen Spieler anhand seiner ID aus der Datenbank.
     */
    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable UUID id) {
        playerRepository.deleteById(id);
    }

    /**
     * Aktualisiert die Deckelanzahl eines Spielers direkt in der Datenbank.
     */
    @PutMapping("/{id}/deckel")
    public Player updateDeckel(@PathVariable UUID id, @RequestBody int neueAnzahl) {
        return playerRepository.findById(id)
                .map(p -> {
                    p.setDeckel(neueAnzahl);
                    return playerRepository.save(p);
                })
                .orElse(null);
    }

    /**
     * Setzt alle Spieler in der Datenbank zurück.
     */
    @PostMapping("/reset")
    public List<Player> resetGame() {
        List<Player> allPlayers = playerRepository.findAll();
        allPlayers.forEach(p -> {
            p.setDeckel(0);
            p.setIstSicher(false);
        });
        return playerRepository.saveAll(allPlayers);
    }
}