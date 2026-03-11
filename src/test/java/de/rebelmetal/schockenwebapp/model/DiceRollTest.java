package de.rebelmetal.schockenwebapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiceRollTest {

    @Test
    void testHausnummerVergleich() {
        // Wir erstellen zwei Objekte
        DiceRoll hoch = new DiceRoll(4, 4, 6); // Wird zu 664
        DiceRoll niedrig = new DiceRoll(2, 5, 4); // Wird zu 542

        // Wir berechnen das Ergebnis manuell für die Konsole
        int ergebnis = hoch.compareTo(niedrig);

        // Das hilft uns beim Debuggen:
        System.out.println("Vergleich: 'hoch' gegen 'niedrig'");
        System.out.println("Ergebnis von compareTo: " + ergebnis);

        // WICHTIG: compareTo gibt eine POSITIVE Zahl zurück, wenn das erste Objekt GRÖSSER ist.
        // Ein Test schlägt fehl, wenn die Bedingung (ergebnis > 0) false ist.
        assertTrue(ergebnis > 0, "542 sollte hoeher sein als 432, daher muss das Ergebnis > 0 sein");
    }

    @Test
    void testGleicherWurf() {
        DiceRoll w1 = new DiceRoll(1, 2, 3);
        DiceRoll w2 = new DiceRoll(3, 2, 1);

        // Bei exakt gleichem Wert muss 0 kommen
        assertEquals(0, w1.compareTo(w2), "Identische Wuerfe muessen 0 ergeben");
    }

    @Test
    void testSchockGegenHausnummer() {
        DiceRoll schock = new DiceRoll(1, 1, 2); // Rang 4
        DiceRoll hausnummer = new DiceRoll(6, 6, 5); // Rang 1

        assertTrue(schock.compareTo(hausnummer) > 0, "Schock (Rang 4) muss hoeher sein als Hausnummer (Rang 1)");
    }

    @Test
    void testHierarchie() {
        DiceRoll schockAus = new DiceRoll(1, 1, 1);
        DiceRoll schockZwei = new DiceRoll(2, 1, 1);
        DiceRoll general = new DiceRoll(4, 4, 4);
        DiceRoll strasse = new DiceRoll(6, 5, 4);
        DiceRoll hausnummer = new DiceRoll(6, 6, 5);

        // Schock Aus ist der höchste Rang
        assertTrue(schockAus.compareTo(schockZwei) > 0);
        // Jeder Schock schlägt einen General
        assertTrue(schockZwei.compareTo(general) > 0);
        // General schlägt Straße
        assertTrue(general.compareTo(strasse) > 0);
        // Straße schlägt Hausnummer
        assertTrue(strasse.compareTo(hausnummer) > 0);
    }
    @Test
    void testUngueltigeWerteSolltenExceptionWerfen() {
        // Wir prüfen, ob die Exception geworfen wird, wenn eine 7 dabei ist
        assertThrows(IllegalArgumentException.class, () -> {
            new DiceRoll(7, 1, 1);
        });

        // Wir prüfen, ob es bei einer 0 kracht
        assertThrows(IllegalArgumentException.class, () -> {
            new DiceRoll(0, 4, 2);
        });
    }
}