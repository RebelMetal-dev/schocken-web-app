package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.DiceRoll;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DiceService {
    private final Random random = new Random();

    public DiceRoll rollVirtually() {
        int d1 = random.nextInt(6) + 1; // Generates 0-5, +1 makes it 1-6
        int d2 = random.nextInt(6) + 1;
        int d3 = random.nextInt(6) + 1;

        // We use our 'DiceRoll' model
        return new DiceRoll(d1, d2, d3);
    }

    public DiceRoll rollManually(int d1, int d2, int d3) {
        // Create a DiceRoll object from user-provided values
        return new DiceRoll(d1, d2, d3);
    }
}
