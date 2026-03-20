package de.rebelmetal.schockenwebapp.service;

import de.rebelmetal.schockenwebapp.model.DiceRoll;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DiceService {
    private final Random random = new Random();

    public DiceRoll rollVirtually(boolean hand, int throwCount) {
        int d1 = random.nextInt(6) + 1;
        int d2 = random.nextInt(6) + 1;
        int d3 = random.nextInt(6) + 1;
        return new DiceRoll(d1, d2, d3, hand, throwCount);
    }

    public DiceRoll rollManually(int d1, int d2, int d3, boolean hand, int throwCount) {
        return new DiceRoll(d1, d2, d3, hand, throwCount);
    }
}
