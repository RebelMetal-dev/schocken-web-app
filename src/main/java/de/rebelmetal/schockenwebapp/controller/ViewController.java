package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.model.GameSession;
import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.repository.PlayerRepository;
import de.rebelmetal.schockenwebapp.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller responsible for rendering the main web interface.
 * This class ensures that test data is available in the H2 database
 * to provide a smooth visualization of the game state.
 */
@Controller
@RequiredArgsConstructor
public class ViewController {

    private final GameService gameService;
    private final PlayerRepository playerRepository;

    /**
     * Handles the root request. Creates two test players, persists them,
     * and initializes a new game session for the dashboard view.
     * * @param model the Spring UI model to pass data to Thymeleaf
     * @return the name of the HTML template (index)
     */
    @GetMapping("/")
    public String index(Model model) {
        // Step 1: Create and persist test players to satisfy foreign key constraints
        // Note: In H2 (In-Memory), these are recreated on every application restart
        Player alice = playerRepository.save(new Player("Alice"));
        Player bob = playerRepository.save(new Player("Bob"));

        // Step 2: Initialize a new game session using the valid persisted player IDs
        GameSession session = gameService.createSession(List.of(alice.getId(), bob.getId()));

        // Step 3: Attach the session object to the model for the Thymeleaf template
        model.addAttribute("session", session);
        model.addAttribute("participants", session.getParticipants());

        // Step 4: Return the template name found in src/main/resources/templates/index.html
        return "index";
    }
}