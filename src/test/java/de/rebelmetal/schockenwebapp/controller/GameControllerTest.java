package de.rebelmetal.schockenwebapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.rebelmetal.schockenwebapp.dto.CreateSessionRequest;
import de.rebelmetal.schockenwebapp.model.*;
import de.rebelmetal.schockenwebapp.service.GameService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createSession_returnsCreatedAndGameSessionDTO() throws Exception {
        UUID sessionId = UUID.randomUUID();

        Player alice = new Player("Alice");

        GameParticipant participant = new GameParticipant();
        participant.setId(UUID.randomUUID());
        participant.setPlayer(alice);
        participant.setPenaltyChips(0);
        participant.setThrowCount(0);
        participant.setSafe(false);

        GameSession session = new GameSession();
        session.setId(sessionId);
        session.setPhase(GamePhase.WAITING_FOR_PLAYERS);
        session.setCentralStack(13);
        session.setParticipants(List.of(participant));

        when(gameService.createSession(any())).thenReturn(session);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateSessionRequest(List.of(UUID.randomUUID())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.phase").value("WAITING_FOR_PLAYERS"))
                .andExpect(jsonPath("$.centralStack").value(13))
                .andExpect(jsonPath("$.participants[0].playerName").value("Alice"));
    }

    @Test
    void performRoll_returnsOkAndParticipantDTOWithThrowCountAndSafe() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        Player bob = new Player("Bob");
        DiceRoll roll = new DiceRoll(1, 1, 2, true, 1); // Shock 2, hand roll

        GameParticipant participant = new GameParticipant();
        participant.setId(participantId);
        participant.setPlayer(bob);
        participant.setPenaltyChips(3);
        participant.setLastRoll(roll);
        participant.setThrowCount(1);
        participant.setSafe(false);
        participant.setLostFirstHalf(false);
        participant.setLostSecondHalf(false);

        when(gameService.performVirtualRoll(sessionId, participantId)).thenReturn(participant);

        mockMvc.perform(post("/api/sessions/{sessionId}/participants/{participantId}/roll",
                        sessionId, participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName").value("Bob"))
                .andExpect(jsonPath("$.penaltyChips").value(3))
                .andExpect(jsonPath("$.throwCount").value(1))
                .andExpect(jsonPath("$.safe").value(false));
    }

    // --- GET /api/sessions/{id} ---

    @Test
    void getSession_existingId_returns200AndDTO() throws Exception {
        UUID sessionId = UUID.randomUUID();

        Player alice = new Player("Alice");
        GameParticipant participant = new GameParticipant();
        participant.setId(UUID.randomUUID());
        participant.setPlayer(alice);
        participant.setPenaltyChips(0);
        participant.setThrowCount(0);
        participant.setSafe(false);

        GameSession session = new GameSession();
        session.setId(sessionId);
        session.setPhase(GamePhase.WAITING_FOR_PLAYERS);
        session.setCentralStack(13);
        session.setParticipants(List.of(participant));

        when(gameService.getSession(sessionId)).thenReturn(session);

        mockMvc.perform(get("/api/sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.phase").value("WAITING_FOR_PLAYERS"))
                .andExpect(jsonPath("$.centralStack").value(13));
    }

    @Test
    void getSession_unknownId_restClient_returns404WithJsonError() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(gameService.getSession(unknownId))
                .thenThrow(new EntityNotFoundException("Session not found: " + unknownId));

        // REST client: no HX-Request header → GlobalExceptionHandler returns JSON body
        mockMvc.perform(get("/api/sessions/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getSession_unknownId_htmxClient_returnsErrorAlertFragment() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(gameService.getSession(unknownId))
                .thenThrow(new EntityNotFoundException("Session not found: " + unknownId));

        // HTMX client: HX-Request: true → GlobalExceptionHandler returns Thymeleaf fragment
        // Status is 200 because HTMX swaps on 2xx; the error is communicated via rendered HTML.
        mockMvc.perform(get("/api/sessions/{id}", unknownId)
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tavern Error")));
    }
}
