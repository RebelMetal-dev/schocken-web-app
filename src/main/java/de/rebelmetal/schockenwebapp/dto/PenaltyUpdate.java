package de.rebelmetal.schockenwebapp.dto;

/**
 * Data Transfer Object for updating penalty chips.
 * Using a record is modern, immutable, and perfect for simple JSON payloads.
 */
public record PenaltyUpdate(int amount) {
}
