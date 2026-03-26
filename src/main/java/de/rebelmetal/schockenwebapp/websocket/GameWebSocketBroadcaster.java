package de.rebelmetal.schockenwebapp.websocket;

import de.rebelmetal.schockenwebapp.dto.GameStateDTO;
import de.rebelmetal.schockenwebapp.event.GameStateChangedEvent;
import de.rebelmetal.schockenwebapp.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for GameStateChangedEvents and broadcasts the filtered session state
 * to all WebSocket subscribers of the session's topic.
 *
 * Why @TransactionalEventListener(AFTER_COMMIT):
 *   The event is published inside a @Transactional method in GameService.
 *   Processing it AFTER_COMMIT guarantees that getSessionState() reads the
 *   committed database state — not a snapshot from an open transaction.
 *
 * SRP: This class knows about WebSocket. GameService does not.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameStateChanged(GameStateChangedEvent event) {
        GameStateDTO state = gameService.getSessionState(event.getSessionId());
        String destination = "/topic/sessions/" + event.getSessionId() + "/state";
        messagingTemplate.convertAndSend(destination, state);
        log.debug("Broadcast GameStateDTO to {} — phase={}, activeIndex={}",
                destination, state.phase(), state.activeParticipantIndex());
    }
}
