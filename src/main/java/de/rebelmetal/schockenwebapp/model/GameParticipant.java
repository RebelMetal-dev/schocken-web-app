package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * The "bridge" or "join-entity" between Player and GameSession.
 * It holds the transient state of a player within a SPECIFIC game session.
 * This prevents "state leakage" and maintains a clean separation of concerns.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GameParticipant {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private GameSession session;

    /**
     * Penalty chips (formerly 'deckel' in Player).
     */
    private int penaltyChips;

    private boolean safe;
    
    /**
     * If the player rolled the dice "blindly".
     */
    private boolean blind;

    /**
     * The last roll within the current turn.
     */
    @Embedded
    @AttributeOverride(name = "dice", column = @Column(name = "last_roll"))
    private DiceRoll lastRoll;

    private boolean lostFirstHalf;
    private boolean lostSecondHalf;

    /**
     * Business Logic: Check if the player has already lost the whole match.
     */
    public boolean hasLostMatch() {
        return lostFirstHalf && lostSecondHalf;
    }
}
