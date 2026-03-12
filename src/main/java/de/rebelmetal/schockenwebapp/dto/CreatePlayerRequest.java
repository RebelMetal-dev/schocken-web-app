package de.rebelmetal.schockenwebapp.dto;

/**
 * Data Transfer Object for player creation.
 * We only need the player's name to start a game.
 */
public record CreatePlayerRequest(String name) {
}
