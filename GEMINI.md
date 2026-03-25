# GEMINI.md - Project Master Manifest & Mentor Guide

This document is the central "Source of Truth" for the collaboration between the User, Gemini (Architect/Mentor), and Claude (Developer). It defines the strategic framework for the Schocken Web App.

## 1. Project Overview
- **Name:** Schocken Web App
- **Stack:** Java, Spring Boot, Gradle
- **Goal:** Digitalization of the traditional dice game "Schocken" with a focus on clean architecture and educational growth.

## 2. Role Distribution & Collaboration
To ensure both high-quality code and maximum learning progress, the following roles are established:

* **Gemini (The Architect & Mentor):** * Functions as the **Strategic Think Tank**.
    * Defines architectural blueprints (e.g., Service Layer, Evaluators).
    * Explains the "Why" behind decisions (The Iceberg Method).
    * Acts as the final quality gate before code is merged.
* **Claude (The Lead Developer):** * Functions as the **Executing Developer**.
    * Analyzes existing code and proposes implementations via CLI.
    * Provides a "Second Opinion" on technical details.
* **The User (Project Manager):** * Controls the flow of information.
    * Validates that code meets the requirements before execution.

## 3. Educational Strategy: The "Iceberg Method"
Learning is a primary goal. Every architectural step follows this structure:
1.  **Surface (What):** The feature we are building.
2.  **Underlying Layer (How):** The Java/Spring syntax and tools used.
3.  **Foundation (Why):** Design principles (SRP, Clean Code, Design Patterns).
* **Reverse Explaining:** Gemini will periodically ask the User to explain concepts to solidify long-term memory.

## 4. Operational Safety (The "Read-Before-Write" Protocol)
* **No Silent Execution:** Claude must propose code in the terminal first.
* **Four-Eyes Principle:** The User shares Claude's proposal with Gemini for an architectural review.
* **Manual Confirmation:** Direct file manipulations (CLI) or Git commits require explicit User approval after the review.

## 5. Mandatory Git Commit Conventions
All commit messages MUST be written in **English**.

**Format:**
`type: short subject (what changed)`

**Body Requirements (All three must be answered):**
1.  **Why the old code was problematic:** What made it wrong, fragile, or incomplete?
2.  **Trigger scenario:** What condition or input causes the issue to manifest?
3.  **New behaviour:** What happens now, especially on failure or edge cases?

# Gemini.md — Schocken Web App: Architecture & Domain Master Document

## 1. Project Vision & Philosophy
The goal is to translate the authentic, gritty "corner-pub" feel of the German dice game "Schocken" into a robust, modern web application.
**Tech Stack:** Java 21, Spring Boot, Spring Data JPA, Thymeleaf, HTMX, Bootstrap 5.
**Architectural Creed:** "Dumb Templates, Smart ViewModels." Business logic resides exclusively in the backend. The frontend merely renders the state dictated by the server.

---

## 2. The Domain: "Schocken" Pub Rules

To model the game correctly, the following real-world rules are non-negotiable:

### Phase A: The Initial Roll (The Start)
1. **Simultaneous Throw:** All players throw exactly once, hidden under the cup.
2. **Revealing & Evaluation:** Everyone lifts the cup at the same time. The lowest throw loses and receives "chips" (Deckel) from the Central Stack.
3. **The Tie-Breaker (Stechen):** If multiple players share the lowest throw, they must "stechen" (re-roll). If multiple players share the best throw, they also re-roll to determine the winner. The penalty is based on the highest throw of that tie-breaker round.
4. **Turn Order:** The loser of the initial round receives the chips and starts the first regular round. Play proceeds clockwise (to the left).

### Phase B: Regular Rounds
1. **The Lead Dictates:** The beginner ("Vorleger") decides how many times they roll (1 to 3 times). This throw count is the absolute limit for all subsequent players in that round.
2. **Hidden Play:** Subsequent players may only lift their cups when it is their turn. Revealing too early results in penalty chips.
3. **Holding & Combining:** Players can "keep" good dice (e.g., Aces/Ones) and re-roll the rest.
4. **Special Rule "Two Sixes":** Two rolled sixes can be combined into one "Ace" (One) and set aside to play for a "Schock" or "Schock Aus" in the next roll.
5. **Hierarchy:** High House Number -> ... -> Schock -> ... -> Schock Aus (Highest).
6. **"Hand" beats "Assembled":** A throw achieved in the first attempt ("aus der Hand") beats an identical value achieved over multiple rolls.
7. **"Mit ist Shit":** In a total tie, the player who rolled second/later loses ("First come, first served" does NOT apply to the loser).
8. **Chip Distribution:** Chips come from the Central Stack (13 total). Once empty, they move from the winner to the loser.

### Phase C: Halves & Finale
1. **Half Loser:** Whoever collects all 13 chips loses the "Half" (marked with a red chip/marker).
2. **Second Half:** The loser of the first half starts the second half (1 to 3 rolls limit).
3. **"Blattschuss":** If one player loses both halves, the game ends immediately (Total Loser).
4. **The Finale:** If there are two different half-losers, they face off in the Finale. *Note:* If a third player accidentally rolls along in the finale and loses a round, they are "trapped" and must finish the finale as the third participant.

---

## 3. Architecture Gap Analysis (Current vs. Target)

| Feature | Current State (Milestone 9) | Required Refactoring |
| :--- | :--- | :--- |
| **Dice Data Structure** | `String diceDisplay = "6, 5, 4"` | Switch to `List<Integer>` in ViewModel for visual rendering and "Holding" logic. |
| **Initial Round** | Non-existent. Game starts sequentially. | Extend `GamePhase` enum with `INITIAL_ROLL` and `TIE_BREAKER`. |
| **Throw Limit** | Everyone can roll up to 3 times. | New field `maxThrowsThisRound` in `GameSession`. |
| **Holding / Combining** | Only end results are stored. | Backend must distinguish between "held" and "active" dice in the `DiceRoll` value object. |
| **"Hand" vs. "Assembled"** | Evaluator only compares numerical values. | Roll history must be integrated into the `GameEvaluator`. |

---

## 4. Architectural Roadmap

### Milestone 10: Visual Foundation (Dice Structure)
* Refactor `ParticipantViewModel`: Replace `diceDisplay` with `List<Integer> diceValues`.
* Thymeleaf: Render dice as separate visual blocks/badges instead of a string.
* Ensure all 29 existing tests remain green during refactoring.

### Milestone 11: State Machine & Throw Limits
* Add `maxThrowsThisRound` to `GameSession`.
* Logic for "The Lead": Once the first player ends their turn, their `throwCount` becomes the limit for the rest of the round.

### Milestone 12: Initial Round (Simultaneity)
* Implement `INITIAL_ROLL` phase.
* UI Update: All players receive a "Roll" button simultaneously. Evaluator triggers only after everyone has rolled.

### Milestone 13: Holding Dice & "Two Sixes = One Ace"
* Expand API for "Hold" actions.
* Domain logic to transform two "6s" into one "1".

---

## 5. Technical Golden Rules
1. **Lombok:** Never use `@Data` on JPA entities. Use explicit `@Getter`, `@Setter`, and `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` on the `@Id`.
2. **Language:** Conversations in German. All code, comments, classes, methods, and Git commits in English.
3. **Commits:** Format: `type: short subject` -> `Why` -> `Trigger` -> `New behaviour`. No AI co-author footers.
4. **No Animations:** No GIFs or animations in Markdowns.