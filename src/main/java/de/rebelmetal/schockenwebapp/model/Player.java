package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data model for a player in the "Schocken" game.
 * Renamed to professional English standards while maintaining 
 * database backward compatibility via @Column and @AttributeOverride.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Player {

    @Id
    private UUID id;

    private String name;

    /**
     * Penalty chips (formerly 'deckel').
     * Mapping to DB column 'deckel' for backward compatibility.
     */
    @Column(name = "deckel")
    private int penaltyChips;

    /**
     * Safety status (formerly 'istSicher').
     * Renamed to 'safe' (Lombok generates 'isSafe()').
     */
    @Column(name = "istSicher")
    private boolean safe;

    /**
     * The result of the last dice roll (formerly 'letzterWurf').
     * Overriding the embedded field 'dice' to map to 'letzterWurf'.
     */
    @Embedded
    @AttributeOverride(name = "dice", column = @Column(name = "letzterWurf"))
    private DiceRoll lastRoll;
}
