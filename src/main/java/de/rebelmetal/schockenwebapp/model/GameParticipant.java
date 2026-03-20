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
 * Join entity between Player and GameSession.
 * Stores the transient state of a player within a specific game.
 * This ensures the core Player entity remains clean and reusable.
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
     * Penalty chips currently held by this participant.
     */
    private int penaltyChips;

    /**
     * Flag indicating if the player is safe for the current round.
     */
    private boolean safe;
    
    /**
     * Flag indicating if the player rolled without looking ("blind").
     */
    private boolean blind;

    /**
     * The last dice roll performed in the current turn.
     */
    @Embedded
    @AttributeOverride(name = "dice", column = @Column(name = "last_roll"))
    private DiceRoll lastRoll;

    private boolean lostFirstHalf;
    private boolean lostSecondHalf;

    /**
     * Number of throws used to achieve the current roll (1 = hand-roll, 2–3 = combined).
     */
    private int throwCount;

    /**
     * Business Logic: Determine if the player has lost both halves.
     * @return true if both halves are lost.
     */
    public boolean hasLostMatch() {
        return lostFirstHalf && lostSecondHalf;
    }

    /**
     * Clears the last roll and throw count at the start of a new round.
     */
    public void resetRoll() {
        this.lastRoll = null;
        this.throwCount = 0;
    }
}
