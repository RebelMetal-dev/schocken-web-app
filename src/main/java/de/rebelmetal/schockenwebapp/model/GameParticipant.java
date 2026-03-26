package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Join entity between Player and GameSession.
 * Stores the transient state of a player within a specific game.
 * This ensures the core Player entity remains clean and reusable.
 */
// @Data is an anti-pattern on JPA entities — its generated equals()/hashCode()
// covers all fields, causing Hibernate dirty-check issues when field values change
// (e.g. lastRoll changes from null to a DiceRoll after performVirtualRoll).
// @EqualsAndHashCode(onlyExplicitlyIncluded = true) restricts comparison to @Id only:
// two participants are equal if and only if they share the same database identity.
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GameParticipant {

    @Id
    @EqualsAndHashCode.Include
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
     * True once the player has physically revealed their cup (SCHOCKEN_RULES.md §3.2).
     * Distinct from blind: a player can choose to play blind but still have their cup
     * revealed during the sequential showdown.
     * Reset to false at the start of each new round via resetRoll().
     */
    private boolean cupRevealed = false;

    /**
     * The last dice roll performed in the current turn.
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "dice",       column = @Column(name = "last_roll")),
        @AttributeOverride(name = "hand",       column = @Column(name = "roll_is_hand")),
        @AttributeOverride(name = "throwCount", column = @Column(name = "roll_throw_count"))
    })
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
        this.cupRevealed = false;
    }
}
