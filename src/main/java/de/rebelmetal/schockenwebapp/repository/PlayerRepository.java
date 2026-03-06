package de.rebelmetal.schockenwebapp.repository;

import de.rebelmetal.schockenwebapp.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Repository-Interface für den Zugriff auf die Spieler-Datenbank.
 * Durch das Erben von JpaRepository stehen automatisch Methoden wie
 * save(), findAll() und deleteById() zur Verfügung.
 * * Das Interface nutzt Lombok-gestützte Player-Objekte und UUIDs als Primärschlüssel.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    // Hier ist aktuell kein eigener Code nötig – Spring Boot erledigt den Rest!
}
