package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Core domain entity for a Player.
 * Represents a person's identity in the system.
 * Transient game state is handled by GameSession/GameParticipant.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    /**
     * Constructor for creating a new player with a name.
     * The ID will be generated upon persistence.
     */
    public Player(String name) {
        this.name = name;
    }

}
