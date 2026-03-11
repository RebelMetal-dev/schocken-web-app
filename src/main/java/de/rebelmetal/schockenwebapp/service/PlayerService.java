package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.DiceRoll;
import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service-Klasse für die Geschäftslogik der Spielerverwaltung.
 * Trennt die Datenbankzugriffe (Repository) von der Web-Schicht (Controller).
 */
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;


    private final DiceService diceService;

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player createPlayer(String name) {
        Player player = new Player(UUID.randomUUID(), name, 0, false,null);
        return playerRepository.save(player);
    }

    public void deletePlayer(UUID id) {
        playerRepository.deleteById(id);
    }

    /**
     * Erhöht die Deckelanzahl eines Spielers.
     * Hier könnten später Spielregeln geprüft werden (z.B. max. 13 Deckel).
     */
    public Player addDeckel(UUID id, int anzahl) {
        return playerRepository.findById(id).map(p -> {
            p.setDeckel(p.getDeckel() + anzahl);
            return playerRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));
    }

    /**
     * Lässt einen Spieler virtuell würfeln (Zufall).
     */
    public DiceRoll performVirtualRoll(UUID playerId) {
        // 1. Spieler in der Datenbank suchen
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));

        // 2. Den DiceService würfeln lassen
        DiceRoll roll = diceService.rollVirtually();

        // 3. Kurzes Feedback in der Konsole
        System.out.println("Spieler " + player.getName() + " hat virtuell gewürfelt: " + roll.getType());

        return roll;
    }

    /**
     * Registriert einen manuellen Wurf (echte Würfel vom Tisch).
     */
    public DiceRoll performManualRoll(UUID playerId, int d1, int d2, int d3) {
        // 1. Spieler suchen
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));

        // 2. Den DiceService nutzen, um die Eingabe zu verarbeiten
        DiceRoll roll = diceService.rollManually(d1, d2, d3);

        System.out.println("Manueller Wurf für " + player.getName() + " registriert: " + roll.getType());

        return roll;
    }
}
