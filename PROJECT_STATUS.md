# Project Status: Schocken Web App (Refactoring & Game Logic)

##  Current Project Phase
Phase 1 (Foundation) completed. Transitioning to Phase 2 (Business Logic).

##  Completed Tasks (The Goldstandard)
- **Internationalization**: Full migration from German/Denglisch to English naming conventions (e.g., `penaltyChips` instead of `deckel`).
- **Domain Modeling**: `Player` entity is refactored; `DiceRoll` is an `@Embeddable` Value Object.
- **API Design**: `PlayerController` uses RESTful principles with `ResponseEntity` and DTOs (`PenaltyUpdate`).
- **Clean Code**: Business logic logging moved to `PlayerService`; models are logic-free.
- **Persistence**: Database mapping via JPA/Hibernate fixed with `@AttributeOverride` to maintain legacy DB compatibility.

## 🛠 Project Context & Stand
- **Language**: 100% English for code, comments, and architecture.
- **Framework**: Spring Boot 3.x, JPA, H2 Database.
- **State**: The application can manage players and basic chip counts but lacks game-specific rules (rounds/halves).

##  Roadmap & Pending Tasks (Backlog)

### 1. Domain Model Expansion (Next Step)
- [ ] Add `boolean lostFirstHalf` to `Player` entity.
- [ ] Add `boolean lostSecondHalf` to `Player` entity.
- [ ] Implement `hasLostMatch()` method to identify the "Final Loser".

### 2. Game Session Logic
- [ ] Implement a `GameSession` or `GameService` to track the `centralStack` (13 chips).
- [ ] Develop `handleShockOut(UUID loserId)`:
    - Must be `@Transactional`.
    - Must transfer all remaining stack chips to the loser.
    - Must mark the current half as lost for that player.

### 3. Validation & Error Handling
- [ ] Implement `IllegalGameMoveException` for invalid actions (e.g., adding chips when the stack is empty).
- [ ] Create a `@ControllerAdvice` for global API error mapping.

### 4. Scoring Engine
- [ ] Create `ScoringService` to rank dice results (Shock Out > General > Shock > Points).
