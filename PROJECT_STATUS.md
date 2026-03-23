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
    * `findWinner(List<GameParticipant>)` — FIFO tie-break: earlier roller wins.
    * `findAllLowestRollers(List<GameParticipant>)` — returns all tied lowest rollers for setup-phase tie detection.
    * `calculatePenalty(DiceRoll)` — delegates to `DiceRoll.getPenaltyValue()` (DRY, single source of truth).
* **DiceRoll — Penalty Logic (Completed ✅):**
    * `getPenaltyValue()` — translates dice result into penalty chip count.
    * `isShockOut()` — exposes half-time trigger without leaking the semantic constant 13.
    * 10 test cases verified via `@ParameterizedTest`.
* **IntegerListConverter (Completed ✅):**
    * `@AttributeConverter` mapping `List<Integer>` (dice values) to a CSV string column in the database.
* **GameService (Completed ✅):**
    * Session creation with bulk player fetch and integrity check.
    * Virtual and manual roll registration via `DiceService`.
    * `evaluateSetupAndDetermineOrder(UUID, List<UUID>)` — determines starting player order:
        * Single loser → reorders participants starting with loser, transitions to `FIRST_HALF`.
        * Tied losers → clears their rolls, transitions to `SETTING_UP_ORDER` (Stechen).
    * Round evaluation via `RoundEvaluator` — winner/loser determined internally (not passed from outside).
    * Chip distribution: Phase 1 (central stack) → Phase 2 (winner-to-loser).
    * Automatic `GamePhase.SECOND_HALF` transition on SHOCK_OUT or empty stack.

## 4. Milestone 3: API & Web Interface (Planned ⏳)
* **REST Controllers:** Exposing game actions to the frontend.
* **DTO Mapping:** Ensuring internal entities are not leaked directly to the web.
* **Error Handling:** Global exception handling for game rule violations.

## 4. Milestone 3: Testing & Stability (Completed ✅)
* **`GameServiceIT`** — 4-player integration test: Shock 2 wins, House Number loses, 2 chips distributed from stack.
* **`GameServiceIntegrationTest`** — 3-test suite covering setup tie-break (SETTING_UP_ORDER), Shock Out chip transfer, and Phase 2 winner-to-loser redistribution.
* **`Player` entity fixed** — `@GeneratedValue(UUID)` added; `Player(String name)` constructor added to ensure correct JPA `persist()` lifecycle.
* All 23 tests passing.

## 5. Milestone 4: Game Flow Completion (Planned ⏳)

The service layer currently handles individual rounds but has no concept of a complete
game arc. The following must be implemented in order before REST endpoints are meaningful:

### 4a. Virtual Roll Storage + Round Completion Guard *(combined — Architect: Gemini)*
* Add `performVirtualRoll(UUID sessionId, UUID participantId)` to `GameService`.
* `DiceService.rollVirtually()` currently returns a `DiceRoll` object only — no service
  method persists it to the DB. A virtual roll must be stored like a manual roll.
* **4a.1 — Null-Guard (combined):** Add a precondition to `evaluateRoundAndDistributeChips`
  that checks `getLastRoll() != null` for all rollers before evaluation begins. This is
  implemented in the same step as virtual roll storage — testing 4a immediately surfaces
  null-roll scenarios when not all players have rolled yet. *(Resolves Technical Debt, Section 6)*

### 4b. Round Reset
* `GameParticipant.resetRoll()` exists but is never called automatically.
* After each round evaluation, all participants must have their `lastRoll` and
  `throwCount` cleared before the next round begins.

### 4c. Half-Time Loss Tracking
* `lostFirstHalf` and `lostSecondHalf` flags exist on `GameParticipant` but are never set.
* `GameService` must set `lostFirstHalf = true` on the loser when `SECOND_HALF` is entered,
  and `lostSecondHalf = true` on the loser at the end of the second half.

### 4d. FINAL_MATCH Logic
* When a participant has `lostFirstHalf && lostSecondHalf` (`hasLostMatch() == true`),
  they enter the final match.
* Transition to `GamePhase.FINAL_MATCH` and determine the two finalists.

### 4e. GAME_OVER Condition
* After the final match, determine the ultimate loser and transition to `GAME_OVER`.

### 4f. REST Controllers & DTO Mapping
* Only build controllers after 4a–4e are complete and tested.
* Expose game actions via REST; map internal entities to DTOs to avoid leaking domain objects.
* Global exception handling for rule violations (e.g. rolling out of turn).

## 6. Technical Debt & Notes
* *Note:* Ensure all future business logic remains in Services/Evaluators, not in Entities (SRP).
* *Note:* Maintain 100% English naming convention for all new components.
* *Note:* `@NoArgsConstructor(force = true)` on `DiceRoll` creates a JPA-only constructor with null dice — never call `new DiceRoll()` directly in production code.
* *Tech Debt:* `RoundEvaluator` has no null-guard for `getLastRoll()`. Calling `evaluateRoundAndDistributeChips` before all participants have rolled causes a silent NPE. A precondition check (throw `IllegalStateException` if any roll is null) should be added before Phase 4 REST endpoints are exposed.
