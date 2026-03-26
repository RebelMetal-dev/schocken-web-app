package de.rebelmetal.schockenwebapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures the STOMP-over-WebSocket message broker.
 *
 * Channel conventions (laut SCHOCKEN_RULES.md §3):
 *   /topic/sessions/{sessionId}/state  — server broadcasts filtered GameStateDTO to all subscribers
 *   /app/...                           — reserved for future client-to-server STOMP commands
 *
 * The simple in-memory broker is sufficient for a single-node deployment.
 * Switch to a full broker (e.g. RabbitMQ) when horizontal scaling is required.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable the simple in-memory broker for /topic destinations.
        // All broadcast messages land here; STOMP clients subscribe via /topic/...
        registry.enableSimpleBroker("/topic");

        // Prefix for messages routed to @MessageMapping methods in controllers.
        // Keeps server-bound commands clearly separated from broadcast channels.
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket handshake endpoint. Clients connect to ws://host/ws.
        // withSockJS() provides a fallback for browsers that do not support WebSocket natively.
        registry.addEndpoint("/ws").withSockJS();
    }
}
