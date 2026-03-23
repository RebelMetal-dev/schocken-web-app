package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Immutable Value Object representing a single dice roll (3 dice).
 * Encapsulates sorting and ranking logic for the game "Schocken".
 */
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(force = true)
@ToString
public class DiceRoll implements Comparable<DiceRoll> {

    private final List<Integer> dice;
    private final boolean hand;
    private final int throwCount;

    public DiceRoll(int d1, int d2, int d3) {
        this(d1, d2, d3, false, 1);
    }

    public DiceRoll(int d1, int d2, int d3, boolean hand, int throwCount) {
        if (IntStream.of(d1, d2, d3).anyMatch(n -> n < 1 || n > 6)) {
            throw new IllegalArgumentException("Dice values must be between 1 and 6!");
        }
        if (throwCount < 1 || throwCount > 3) {
            throw new IllegalArgumentException("Throw count must be between 1 and 3!");
        }
        this.dice = IntStream.of(d1, d2, d3)
                .boxed()
                .sorted(Collections.reverseOrder())
                .toList();
        this.hand = hand;
        this.throwCount = throwCount;
    }

    public List<Integer> getDice() { return dice; }
    public boolean isHand() { return hand; }
    public int getThrowCount() { return throwCount; }

    /**
     * Evaluates the category of this roll based on the sorted dice values.
     * @return The RollType for this roll.
     */
    public RollType getType() {
        if (dice.get(0) == 1 && dice.get(1) == 1 && dice.get(2) == 1) return RollType.SHOCK_OUT;
        if (dice.get(1) == 1 && dice.get(2) == 1) return RollType.SHOCK;
        if (dice.get(0).equals(dice.get(1)) && dice.get(1).equals(dice.get(2))) return RollType.TRIPLET;
        if (dice.get(0) == dice.get(1) + 1 && dice.get(1) == dice.get(2) + 1) return RollType.STRAIGHT;
        return RollType.HOUSE_NUMBER;
    }

    /**
     * Calculates the penalty value (Strafsteine) for this roll.
     * SHOCK_OUT = 13, SHOCK = high dice, TRIPLET = 3, STRAIGHT = 2, HOUSE_NUMBER = 1.
     */
    public int getPenaltyValue() {
        return switch (getType()) {
            case SHOCK_OUT -> 13;
            case SHOCK -> dice.get(0); // Since sorted [x, 1, 1], x is the value
            case TRIPLET -> 3;
            case STRAIGHT -> 2;
            case HOUSE_NUMBER -> 1;
        };
    }

    /**
     * Helper to check if this roll ends the half-time immediately.
     */
    public boolean isShockOut() {
        return getType() == RollType.SHOCK_OUT;
    }

    @Override
    public int compareTo(DiceRoll other) {
        // 1. Roll type rank (SHOCK_OUT > SHOCK > TRIPLET > STRAIGHT > HOUSE_NUMBER)
        int rankCompare = Integer.compare(this.getType().rank, other.getType().rank);
        if (rankCompare != 0) return rankCompare;

        // 2. Hand beats non-hand (single throw beats combined)
        int handCompare = Boolean.compare(this.hand, other.hand);
        if (handCompare != 0) return handCompare;

        // 3. Fewer throws beat more throws
        int throwCompare = Integer.compare(other.throwCount, this.throwCount);
        if (throwCompare != 0) return throwCompare;

        // 4. Actual dice values
        int myValue = this.dice.get(0) * 100 + this.dice.get(1) * 10 + this.dice.get(2);
        int otherValue = other.dice.get(0) * 100 + other.dice.get(1) * 10 + other.dice.get(2);
        return Integer.compare(myValue, otherValue);
    }
}
