# Schocken Web App: Project Status & Roadmap

## 1. Project Vision
A Spring Boot-based digitalization of the "Schocken" dice game, following Clean Architecture and DDD (Domain-Driven Design) principles.

## 2. Milestone 1: Core Domain (Completed ✅)
* **Player Entity:** Established as a pure identity (ID & Name).
* **DiceRoll Value Object:** Implemented with immutable ranking logic, comparison, penalty calculation, and SHOCK_OUT detection.
* **GameParticipant:** Created to hold transient session state (chips, rolls, flags).
* **GameSession:** Established as the root context for a gaming round.

## 3. Milestone 2: Business Logic & Service Layer (Completed ✅)
* **RoundEvaluator:** Implemented as a Strategy component. Determines the loser of a round via `findLoser(List<GameParticipant>)` based on `DiceRoll` comparisons (Schock-Rang > Hand-Bonus > LIFO).
* **DiceRoll — Penalty Logic (Completed ✅):**
    * `getPenaltyValue()` — translates dice result into penalty chip count.
    * `isShockOut()` — exposes half-time trigger without leaking the semantic constant 13.
    * 10 test cases verified via `@ParameterizedTest`.
* **GameService (Completed ✅):**
    * Session creation with bulk player fetch and integrity check.
    * Virtual and manual roll registration via `DiceService`.
    * Round evaluation via `RoundEvaluator` — winner/loser determined internally (not passed from outside).
    * Chip distribution: Phase 1 (central stack) → Phase 2 (winner-to-loser).
    * Automatic `GamePhase.SECOND_HALF` transition on SHOCK_OUT or empty stack.

## 4. Milestone 3: API & Web Interface (Planned ⏳)
* **REST Controllers:** Exposing game actions to the frontend.
* **DTO Mapping:** Ensuring internal entities are not leaked directly to the web.
* **Error Handling:** Global exception handling for game rule violations.

## 5. Technical Debt & Notes
* *Note:* Ensure all future business logic remains in Services/Evaluators, not in Entities (SRP).
* *Note:* Maintain 100% English naming convention for all new components.
* *Note:* `@NoArgsConstructor(force = true)` on `DiceRoll` creates a JPA-only constructor with null dice — never call `new DiceRoll()` directly in production code.
