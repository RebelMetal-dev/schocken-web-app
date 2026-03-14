package de.rebelmetal.schockenwebapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiceRollTest {

    @Test
    void testHouseNumberComparison() {
        // We create two objects
        DiceRoll high = new DiceRoll(4, 4, 6); // Becomes 644
        DiceRoll low = new DiceRoll(2, 5, 4);  // Becomes 542

        int result = high.compareTo(low);

        // IMPORTANT: compareTo returns a POSITIVE number if the first object is GREATER.
        assertTrue(result > 0, "644 should be higher than 542, so the result must be > 0");
    }

    @Test
    void testIdenticalRolls() {
        DiceRoll w1 = new DiceRoll(1, 2, 3);
        DiceRoll w2 = new DiceRoll(3, 2, 1);

        // For exactly the same value, it must return 0
        assertEquals(0, w1.compareTo(w2), "Identical rolls must result in 0");
    }

    @Test
    void testShockAgainstHouseNumber() {
        DiceRoll shock = new DiceRoll(1, 1, 2);    // Rank 4
        DiceRoll houseNumber = new DiceRoll(6, 6, 5); // Rank 1

        assertTrue(shock.compareTo(houseNumber) > 0, "Shock (Rank 4) must be higher than House Number (Rank 1)");
    }

    @Test
    void testHierarchy() {
        DiceRoll shockOut = new DiceRoll(1, 1, 1);
        DiceRoll shockTwo = new DiceRoll(2, 1, 1);
        DiceRoll triplet = new DiceRoll(4, 4, 4);
        DiceRoll straight = new DiceRoll(6, 5, 4);
        DiceRoll houseNumber = new DiceRoll(6, 6, 5);

        // Shock Out is the highest rank
        assertTrue(shockOut.compareTo(shockTwo) > 0);
        // Any Shock beats a Triplet
        assertTrue(shockTwo.compareTo(triplet) > 0);
        // Triplet beats Straight
        assertTrue(triplet.compareTo(straight) > 0);
        // Straight beats House Number
        assertTrue(straight.compareTo(houseNumber) > 0);
    }

    @Test
    void testInvalidValuesShouldThrowException() {
        // Check if exception is thrown when a 7 is provided
        assertThrows(IllegalArgumentException.class, () -> {
            new DiceRoll(7, 1, 1);
        });

        // Check if it fails for a 0
        assertThrows(IllegalArgumentException.class, () -> {
            new DiceRoll(0, 4, 2);
        });
    }
}
