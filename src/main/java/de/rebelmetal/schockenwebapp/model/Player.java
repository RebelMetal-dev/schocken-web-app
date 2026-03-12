package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Das Datenmodell für einen Spieler im Schocken-Spiel.
 * * Diese Klasse wird als JPA-Entity verwendet, um Spielerdaten persistent in der
 * H2-Datenbank zu speichern. Lombok-Annotationen (@Data, @AllArgsConstructor, @NoArgsConstructor)
 * werden genutzt, um den notwendigen Boilerplate-Code für Getter, Setter und
 * Konstruktoren automatisch zu generieren.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Player {

    /**
     * Eindeutige Identifikationsnummer des Spielers.
     * Dient als Primärschlüssel (@Id) in der Datenbank.
     */
    @Id
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
     * Status, ob der Spieler in der aktuellen Runde sicher ist.
     */
    private boolean istSicher;

    // NEU: Hier speichern wir das Ergebnis des letzten Wurfs
    @Embedded
    private DiceRoll letzterWurf;
}

