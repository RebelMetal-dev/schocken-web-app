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

    public DiceRoll(int d1, int d2, int d3) {
        // Validation: Ensure all dice are within the legal range [1-6]
        if (IntStream.of(d1, d2, d3).anyMatch(n -> n < 1 || n > 6)) {
            throw new IllegalArgumentException("Dice values must be between 1 and 6!");
        }

        // Store dice as an unmodifiable list, sorted descending for easier ranking
        this.dice = IntStream.of(d1, d2, d3)
                .boxed()
                .sorted(Collections.reverseOrder())
                .toList();
    }

    /**
     * Returns the unmodifiable list of dice.
     * @return List of integers representing the three dice.
     */
    public List<Integer> getDice() {
        return dice;
    }

    /**
     * Identifies the type of the roll based on the sorted values.
     * @return String identifier like "SHOCK_OUT", "TRIPLET", or "HOUSE_NUMBER".
     */
    public String getType() {
        if (dice.get(0) == 1 && dice.get(1) == 1 && dice.get(2) == 1) return "SHOCK_OUT";
        if (dice.get(1) == 1 && dice.get(2) == 1) return "SHOCK_" + dice.get(0);
        if (dice.get(0).equals(dice.get(1)) && dice.get(1).equals(dice.get(2))) return "TRIPLET";
        if (dice.get(0) == dice.get(1) + 1 && dice.get(1) == dice.get(2) + 1) return "STRAIGHT";
        
        return "HOUSE_NUMBER";
    }

    /**
     * Internal rank used for comparing different types of rolls.
     * @return Integer representing the power of the roll (higher is better).
     */
    private int getRank() {
        return switch (getType()) {
            case "SHOCK_OUT" -> 5;
            case String s when s.startsWith("SHOCK_") -> 4;
            case "TRIPLET" -> 3;
            case "STRAIGHT" -> 2;
            default -> 1;
        };
    }

    @Override
    public int compareTo(DiceRoll other) {
        // Compare ranks first (e.g., SHOCK beats TRIPLET)
        int rankCompare = Integer.compare(this.getRank(), other.getRank());
        if (rankCompare != 0) return rankCompare;

        // Compare values for "House Numbers" or identical types (e.g., SHOCK 4 vs SHOCK 2)
        int myValue = this.dice.get(0) * 100 + this.dice.get(1) * 10 + this.dice.get(2);
        int otherValue = other.dice.get(0) * 100 + other.dice.get(1) * 10 + other.dice.get(2);

        return Integer.compare(myValue, otherValue);
    }
}
