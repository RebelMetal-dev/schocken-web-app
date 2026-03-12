package de.rebelmetal.schockenwebapp.controller;

import de.rebelmetal.schockenwebapp.dto.CreatePlayerRequest;
import de.rebelmetal.schockenwebapp.dto.PenaltyUpdate;
import de.rebelmetal.schockenwebapp.model.DiceRoll;
import de.rebelmetal.schockenwebapp.model.Player;
import de.rebelmetal.schockenwebapp.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Player API. Delegates all business logic to the PlayerService.
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @PostMapping
    public Player createPlayer(@RequestBody CreatePlayerRequest request) {
        return playerService.createPlayer(request.name());
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable UUID id) {
        playerService.deletePlayer(id);
    }

    @PutMapping("/{id}/penalty-chips")
    public ResponseEntity<Player> updatePenaltyChips(@PathVariable UUID id, @RequestBody PenaltyUpdate update) {

        Player updatedPlayer = playerService.addPenaltyChips(id, update.amount());
        return ResponseEntity.ok(updatedPlayer);
    }

    @PostMapping("/{id}/roll-virtual")
    public DiceRoll rollVirtual(@PathVariable UUID id) {
        // Delegate to service
        return playerService.performVirtualRoll(id);
    }

    @PostMapping("/{id}/roll-manual")
    public DiceRoll rollManual(
            @PathVariable UUID id,
            @RequestParam int d1,
            @RequestParam int d2,
            @RequestParam int d3) {
        // Forward manual entry for processing
        return playerService.performManualRoll(id, d1, d2, d3);
    }

    @PostMapping("/reset-dice")
    public void resetAllDice() {
        playerService.resetAllDice();
    }
}