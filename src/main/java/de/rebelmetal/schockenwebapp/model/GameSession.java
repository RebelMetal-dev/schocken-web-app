package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a specific game of "Schocken".
 * Holds the global state of the match.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class erst den service daGameSession {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private GamePhase phase;

    /**
     * The 13 chips in the middle of the table.
     */
    private int centralStack;

    /**
     * The list of participants currently sitting at this table.
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameParticipant> participants = new ArrayList<>();
}
