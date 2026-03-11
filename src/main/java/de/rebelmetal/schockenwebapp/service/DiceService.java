package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.DiceRoll;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DiceService {
    private final Random random = new Random();

    public DiceRoll rollVirtually() {
        int d1 = random.nextInt(6) + 1; // Erzeugt 0-5, +1 macht daraus 1-6
        int d2 = random.nextInt(6) + 1;
        int d3 = random.nextInt(6) + 1;

        // Wir nutzen unser Model 'DiceRoll'
        return new DiceRoll(d1, d2, d3);
    }

    public DiceRoll rollManually(int d1, int d2, int d3) {
        // Wir nehmen einfach die Zahlen des Users und erstellen ein DiceRoll-Objekt
        return new DiceRoll(d1, d2, d3);
    }
}
