package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a specific game session of "Schocken".
 * Holds the global state and list of participants.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GameSession {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private GamePhase phase;

    /**
     * The number of penalty chips remaining in the central stack (initially 13).
     */
    private int centralStack;

    /**
     * List of participants currently involved in this session.
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "seat_order")
    private List<GameParticipant> participants = new ArrayList<>();

    /**
     * Adds a participant to this session and sets the back-reference.
     * Keeps the bidirectional JPA relationship consistent.
     */
    public void addParticipant(GameParticipant participant) {
        participant.setSession(this);
        this.participants.add(participant);
    }
}
