package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.model.DiceRoll;
import de.rebelmetal.schockenwebapp.model.GameParticipant;

import java.util.UUID;

/**
 * View-layer snapshot of a game participant.
 * Contains only semantic state — no CSS classes, no display strings derived from business rules.
 * Templates are solely responsible for translating state (e.g. safe=true) into visual styles.
 */
public record ParticipantViewModel(
        UUID    id,
        String  displayName,
        String  diceDisplay,    // pre-formatted dice string, e.g. "6, 4, 1" or "—"
        int     throwCount,
        int     penaltyChips,
        boolean safe,
        boolean canRoll,        // false when safe or throw limit reached
        String  avatarUrl,      // null — reserved for future avatar feature
        String  skinId          // reserved for future theming
) {
    private static final int    MAX_THROWS   = 3;
    private static final String DEFAULT_SKIN = "skin-default";

    /**
     * Builds a view model from a JPA entity.
     * All display-readiness logic is centralised here so templates stay logic-free.
     */
    public static ParticipantViewModel from(GameParticipant p) {
        DiceRoll roll = p.getLastRoll();
        String diceDisplay = (roll != null)
                ? roll.getDice().get(0) + ", " + roll.getDice().get(1) + ", " + roll.getDice().get(2)
                : "—";
        boolean safe    = p.isSafe();
        boolean canRoll = !safe && p.getThrowCount() < MAX_THROWS;

        return new ParticipantViewModel(
                p.getId(),
                p.getPlayer().getName(),
                diceDisplay,
                p.getThrowCount(),
                p.getPenaltyChips(),
                safe,
                canRoll,
                null,
                DEFAULT_SKIN
        );
    }
}
