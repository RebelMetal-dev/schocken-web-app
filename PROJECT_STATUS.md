# Schocken Web App: Project Status & Roadmap

## 1. Project Vision
A Spring Boot-based digitalization of the "Schocken" dice game, following Clean Architecture and DDD (Domain-Driven Design) principles.

## 2. Milestone 1: Core Domain (Completed ✅)
* **Player Entity:** Established as a pure identity (ID & Name).
* **DiceRoll Value Object:** Implemented with immutable ranking logic and comparison.
* **GameParticipant:** Created to hold transient session state (chips, rolls, flags).
* **GameSession:** Established as the root context for a gaming round.

## 3. Milestone 2: Business Logic & Service Layer (In Progress 🔄)
* **Current Focus:** Implementation of the `RoundEvaluator`.
    * *Goal:* A specialized component to determine the loser of a round based on `DiceRoll` comparisons.
* **Next Tasks:**
    1.  **GameService:** Orchestrating the game flow (Joining, Rolling, Evaluating).
    2.  **Chip Management:** Logic for distributing penalty chips from the stack to participants.
    3.  **Round Lifecycle:** Handling the transition between individual rolls and round ends.

## 4. Milestone 3: API & Web Interface (Planned ⏳)
* **REST Controllers:** Exposing game actions to the frontend.
* **DTO Mapping:** Ensuring internal entities are not leaked directly to the web.
* **Error Handling:** Global exception handling for game rule violations.

## 5. Technical Debt & Notes
* *Note:* Ensure all future business logic remains in Services/Evaluators, not in Entities (SRP).
* *Note:* Maintain 100% English naming convention for all new components.