package de.rebelmetal.schockenwebapp.model;

/**
 * Defines the current state of a game session.
 * Used to control the flow of the game logic.
 */
public enum GamePhase {
    WAITING_FOR_PLAYERS, // Initial state, players can join
    SETTING_UP_ORDER,    // Players rolling to determine who starts
    FIRST_HALF,          // Current round is for the first half
    SECOND_HALF,         // Current round is for the second half
    FINAL_MATCH,         // Two losers playing for the final match
    GAME_OVER            // Session finished
}
