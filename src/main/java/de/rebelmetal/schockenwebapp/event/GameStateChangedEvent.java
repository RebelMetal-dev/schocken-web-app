package de.rebelmetal.schockenwebapp.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Published by GameService after every state mutation that is visible to clients.
 * Carries only the sessionId — the broadcaster resolves the full state itself,
 * ensuring it always reads the committed database state (not a stale snapshot).
 *
 * Processed by GameWebSocketBroadcaster after the originating transaction commits.
 */
public class GameStateChangedEvent extends ApplicationEvent {

    private final UUID sessionId;

    public GameStateChangedEvent(Object source, UUID sessionId) {
        super(source);
        this.sessionId = sessionId;
    }

    public UUID getSessionId() {
        return sessionId;
    }
}
