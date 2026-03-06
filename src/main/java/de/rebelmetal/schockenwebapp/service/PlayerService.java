package de.rebelmetal.schockenwebapp.service;

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

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player createPlayer(String name) {
        Player player = new Player(UUID.randomUUID(), name, 0, false);
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
}
