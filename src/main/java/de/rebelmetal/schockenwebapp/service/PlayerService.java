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
        Player player = new Player(UUID.randomUUID(), name, 0, false, null);
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

    public DiceRoll performVirtualRoll(UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));

        DiceRoll roll = diceService.rollVirtually();

        player.setLetzterWurf(roll); // 3. Dem Spieler den Wurf zuweisen
        playerRepository.save(player); // 4. Den Spieler SPEICHERN

        System.out.println("Spieler " + player.getName() + " hat gewürfelt: " + roll.getDice());
        return roll;
    }

    public DiceRoll performManualRoll(UUID playerId, int d1, int d2, int d3) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Spieler nicht gefunden"));

        DiceRoll roll = diceService.rollManually(d1, d2, d3);

        player.setLetzterWurf(roll); // 3. Dem Spieler den Wurf zuweisen
        playerRepository.save(player); // 4. Den Spieler SPEICHERN

        return roll;
    }

    public void resetAllDice() {
        // 1. Alle Spieler aus der DB laden
        List<Player> allPlayers = playerRepository.findAll();

        // 2. Jedem Spieler den Wurf wegnehmen
        allPlayers.forEach(player -> player.setLetzterWurf(null));

        // 3. Alle auf einmal speichern
        playerRepository.saveAll(allPlayers);

        System.out.println("Runde beendet: Alle Würfel wurden vom Tisch geräumt.");
    }
}
