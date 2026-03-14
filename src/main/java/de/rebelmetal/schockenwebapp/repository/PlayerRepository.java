package de.rebelmetal.schockenwebapp.repository;

import de.rebelmetal.schockenwebapp.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for accessing the player database.
 * By extending JpaRepository, methods like save(), findAll(), and deleteById() are automatically available.
 * The interface uses Lombok-supported Player objects and UUIDs as primary keys.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    // No custom code needed currently – Spring Boot handles the rest!
}
