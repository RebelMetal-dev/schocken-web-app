package de.rebelmetal.schockenwebapp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.Arrays;

@Getter
@EqualsAndHashCode
public class DiceRoll implements Comparable<DiceRoll> {
    private final int[] dice;

    public DiceRoll(int d1, int d2, int d3) {
        this.dice = new int[]{d1, d2, d3};
        Arrays.sort(this.dice); // Sorgt für 1-2-5 statt 5-1-2
    }

    public String getType() {
        return switch (dice) {
            case int[] d when d[0] == 1 && d[1] == 1 && d[2] == 1 -> "SCHOCK_AUS";
            case int[] d when d[0] == 1 && d[1] == 1 -> "SCHOCK_" + d[2];
            case int[] d when d[0] == d[1] && d[1] == d[2] -> "GENERAL";
            case int[] d when d[1] == d[0] + 1 && d[2] == d[1] + 1 -> "STRASSE";
            default -> "HAUSNUMMER";
        };
    }

    private int getRank() {
        return switch (getType()) {
            case "SCHOCK_AUS" -> 5;
            case String s when s.startsWith("SCHOCK_") -> 4;
            case "GENERAL"    -> 3;
            case "STRASSE"    -> 2;
            default           -> 1;
        };
    }

    @Override
    public int compareTo(DiceRoll other) {
        // 1. Erst den Rang vergleichen (Idee B!)
        int rankCompare = Integer.compare(this.getRank(), other.getRank());
        if (rankCompare != 0) {
            return rankCompare;
        }

        // 2. Wenn der Rang gleich ist: Detailprüfung per Hausnummer-Formel
        // Wir rechnen die Würfel in eine 3-stellige Zahl um (z.B. 5, 4, 2 -> 542)
        int myValue = this.dice[2] * 100 + this.dice[1] * 10 + this.dice[0];
        int otherValue = other.dice[2] * 100 + other.dice[1] * 10 + other.dice[0];

        return Integer.compare(myValue, otherValue);
    }
}