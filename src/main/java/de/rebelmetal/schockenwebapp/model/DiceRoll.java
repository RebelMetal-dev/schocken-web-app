package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


import java.util.Collections;
import java.util.stream.IntStream;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor
@ToString
public class DiceRoll implements Comparable<DiceRoll> {
    private  int[] dice;

    public DiceRoll(int d1, int d2, int d3) {
        // 1. Validation (Defensive mode)
        if (IntStream.of(d1, d2, d3).anyMatch(n -> n < 1 || n > 6)) {
            throw new IllegalArgumentException("Dice values must be between 1 and 6!");
        }

        this.dice = IntStream.of(d1, d2, d3)
                .boxed() // Convert int to Integer object
                .sorted(Collections.reverseOrder())
                .mapToInt(Integer::intValue) // Back to int
                .toArray();
    }

    public String getType() {
        return switch (dice) {
            // Shock Out: [1, 1, 1]
            case int[] d when d[0] == 1 && d[1] == 1 && d[2] == 1 -> "SHOCK_OUT";
            // Shock X: [X, 1, 1] - Since sorted descending, ones are at the end!
            case int[] d when d[1] == 1 && d[2] == 1 -> "SHOCK_" + d[0];
            // Triplet: [X, X, X]
            case int[] d when d[0] == d[1] && d[1] == d[2] -> "TRIPLET";
            // Straight: [X, X-1, X-2] - Descending check!
            case int[] d when d[0] == d[1] + 1 && d[1] == d[2] + 1 -> "STRAIGHT";
            // Rest is house number
            default -> "HOUSE_NUMBER";
        };
    }

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
        // 1. Rank comparison
        int rankCompare = Integer.compare(this.getRank(), other.getRank());
        if (rankCompare != 0) return rankCompare;

        // 2. House number comparison (Descending: Index 0 is the highest number!)
        int myValue = this.dice[0] * 100 + this.dice[1] * 10 + this.dice[2];
        int otherValue = other.dice[0] * 100 + other.dice[1] * 10 + other.dice[2];

        return Integer.compare(myValue, otherValue);
    }
}
