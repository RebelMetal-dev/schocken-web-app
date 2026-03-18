package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.Entity;
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
    private UUID id;

    private String name;
}
