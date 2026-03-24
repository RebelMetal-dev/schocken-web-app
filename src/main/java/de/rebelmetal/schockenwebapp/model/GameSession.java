package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a specific game session of "Schocken".
 * Holds the global state and list of participants.
 */
// @Getter + @Setter instead of @Data — @Data generates equals()/hashCode() over
// all fields, which interferes with Hibernate's dirty-checking on JPA entities.
// Explicit @Getter guarantees Thymeleaf can access id, phase, and centralStack
// via their public getters regardless of proxy state.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GameSession {

    @Id
    private UUID id;

    // Default matches the value set by GameService.createSession() — keeps entity
    // and service in sync, and prevents a partially-constructed instance from
    // exposing a wrong phase to the view layer.
    @Enumerated(EnumType.STRING)
    private GamePhase phase = GamePhase.WAITING_FOR_PLAYERS;

    /**
     * The number of penalty chips remaining in the central stack.
     * Default of 13 mirrors the game rule and matches GameService.createSession().
     * This ensures Thymeleaf always reads a valid value even if accessed on a
     * partially-constructed proxy before the service setter has been called.
     */
    private int centralStack = 13;

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
