package de.rebelmetal.schockenwebapp.model;

/**
 * Represents the category and rank of a Schocken dice roll.
 * Higher rank = better roll.
 */
public enum RollType {
    HOUSE_NUMBER(1),
    STRAIGHT(2),
    TRIPLET(3),
    SHOCK(4),
    SHOCK_OUT(5);

    public final int rank;

    RollType(int rank) {
        this.rank = rank;
    }
}
