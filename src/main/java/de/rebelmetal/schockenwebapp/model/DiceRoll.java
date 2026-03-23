package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(force = true)
@ToString
public class DiceRoll implements Comparable<DiceRoll> {

    @Convert(converter = IntegerListConverter.class)
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
        // Always sort descending for consistent ranking: [6, 4, 1]
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

    public RollType getType() {
        if (dice.get(0) == 1 && dice.get(1) == 1 && dice.get(2) == 1) return RollType.SHOCK_OUT;
        if (dice.get(1) == 1 && dice.get(2) == 1) return RollType.SHOCK;
        if (dice.get(0).equals(dice.get(1)) && dice.get(1).equals(dice.get(2))) return RollType.TRIPLET;
        if (dice.get(0) == dice.get(1) + 1 && dice.get(1) == dice.get(2) + 1) return RollType.STRAIGHT;
        return RollType.HOUSE_NUMBER;
    }

    public int getPenaltyValue() {
        return switch (getType()) {
            case SHOCK_OUT -> 13;
            case SHOCK -> dice.get(0);
            case TRIPLET -> 3;
            case STRAIGHT -> 2;
            case HOUSE_NUMBER -> 1;
        };
    }

    public boolean isShockOut() { return getType() == RollType.SHOCK_OUT; }

    @Override
    public int compareTo(DiceRoll other) {
        if (other == null) return 1;

        // 1. Rank (Enum)
        int rankCompare = Integer.compare(this.getType().rank, other.getType().rank);
        if (rankCompare != 0) return rankCompare;

        // 2. Hand beats combined
        int handCompare = Boolean.compare(this.hand, other.hand);
        if (handCompare != 0) return handCompare;

        // 3. Throw count (Fewer is better -> reverse comparison)
        int throwCompare = Integer.compare(other.getThrowCount(), this.getThrowCount());
        if (throwCompare != 0) return throwCompare;

        // 4. Numerical value (e.g., 654 vs 642)
        int myVal = this.dice.get(0) * 100 + this.dice.get(1) * 10 + this.dice.get(2);
        int otherVal = other.dice.get(0) * 100 + other.dice.get(1) * 10 + other.dice.get(2);
        return Integer.compare(myVal, otherVal);
    }
}