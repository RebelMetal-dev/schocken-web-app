package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.model.Player;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für die Verwaltung der Spielteilnehmer.
 * Ermöglicht den Zugriff auf Spielerdaten und die Steuerung des Spielstatus über HTTP-Requests.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    /**
     * Liste zur Verwaltung der Spieler im Arbeitsspeicher (In-Memory).
     */
    private final List<Player> players = new ArrayList<>();

    /**
     * Ruft alle aktuell am Spiel teilnehmenden Personen ab.
     *
     * @return Eine Liste aller Player-Objekte.
     */
    @GetMapping
    public List<Player> getAllPlayers() {
        return players;
    }

    /**
     * Registriert einen neuen Spieler für die aktuelle Spielrunde.
     *
     * @param name Der Name des Spielers aus dem Request-Body.
     * @return Das neu erstellte Spieler-Objekt mit generierter ID.
     */
    @PostMapping
    public Player addPlayer(@RequestBody String name) {
        Player newPlayer = new Player(UUID.randomUUID(), name, 0, false);
        players.add(newPlayer);
        return newPlayer;
    }

    /**
     * Entfernt einen Spieler permanent aus der Liste.
     *
     * @param id Die UUID des zu löschenden Spielers.
     * @return Die Liste der verbleibenden Spieler nach dem Löschvorgang.
     */
    @DeleteMapping("/{id}")
    public List<Player> deletePlayer(@PathVariable UUID id) {
        players.removeIf(p -> p.getId().equals(id));
        return players;
    }

    /**
     * Ändert die Deckelanzahl eines Spielers basierend auf seiner ID.
     *
     * @param id         Die eindeutige ID des Spielers.
     * @param neueAnzahl Die neue Gesamtzahl der Strafpunkte.
     * @return Das aktualisierte Player-Objekt oder null, falls nicht gefunden.
     */
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

    /**
     * Setzt alle Spieler auf den Initialzustand (0 Deckel, nicht sicher) zurück.
     *
     * @return Die aktualisierte Liste aller Spieler nach dem Reset.
     */
    @PostMapping("/reset")
    public List<Player> resetGame() {
        players.forEach(p -> {
            p.setDeckel(0);
            p.setIstSicher(false);
        });
        return players;
    }
}
