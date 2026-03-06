package de.rebelmetal.schockenwebapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Das Datenmodell für einen Spieler im Schocken-Spiel.
 * Nutzt Lombok-Annotationen, um Boilerplate-Code wie Getter, Setter
 * und Konstruktoren automatisch zu generieren.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    /**
     * Eindeutige Identifikationsnummer des Spielers.
     */
    private UUID id;

    /**
     * Der gewählte Anzeigename des Spielers.
     */
    private String name;

    /**
     * Aktuelle Anzahl der Strafpunkte (Deckel) des Spielers.
     */
    private int deckel;

    /**
     * Status, ob der Spieler in der aktuellen Runde bereits 'raus' bzw. sicher ist.
     */
    private boolean istSicher;
}