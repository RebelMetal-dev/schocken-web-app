package de.rebelmetal.schockenwebapp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

@Getter
@EqualsAndHashCode
public class DiceRoll implements Comparable<DiceRoll> {
    private final int[] dice;

    public DiceRoll(int d1, int d2, int d3) {
        // 1. Validierung (Defensiv-Modus)
        if (IntStream.of(d1, d2, d3).anyMatch(n -> n < 1 || n > 6)) {
            throw new IllegalArgumentException("Würfelwerte müssen zwischen 1 und 6 liegen!");
        }

        this.dice = IntStream.of(d1, d2, d3)
                .boxed() // Macht aus int ein Integer-Objekt
                .sorted(Collections.reverseOrder())
                .mapToInt(Integer::intValue) // Zurück zu int
                .toArray();

        System.out.println("Absteigend sortiert: " + Arrays.toString(this.dice));
    }

    public String getType() {
        return switch (dice) {
            // Schock Aus: [1, 1, 1]
            case int[] d when d[0] == 1 && d[1] == 1 && d[2] == 1 -> "SCHOCK_AUS";
            // Schock X: [X, 1, 1] - Da absteigend sortiert, sind die Einsen hinten!
            case int[] d when d[1] == 1 && d[2] == 1 -> "SCHOCK_" + d[0];
            // General: [X, X, X]
            case int[] d when d[0] == d[1] && d[1] == d[2] -> "GENERAL";
            // Straße: [X, X-1, X-2] - Absteigende Prüfung!
            case int[] d when d[0] == d[1] + 1 && d[1] == d[2] + 1 -> "STRASSE";
            // Rest ist Hausnummer
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
        // 1. Rang-Vergleich
        int rankCompare = Integer.compare(this.getRank(), other.getRank());
        if (rankCompare != 0) return rankCompare;

        // 2. Hausnummer-Vergleich (Absteigend: Index 0 ist die größte Zahl!)
        int myValue = this.dice[0] * 100 + this.dice[1] * 10 + this.dice[2];
        int otherValue = other.dice[0] * 100 + other.dice[1] * 10 + other.dice[2];

        return Integer.compare(myValue, otherValue);
    }



}
