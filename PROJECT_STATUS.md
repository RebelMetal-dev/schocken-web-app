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

## 4. Milestone 3: Testing & Stability (Completed ✅)
* **`GameServiceIT`** — 4-player integration test: Shock 2 wins, House Number loses, 2 chips distributed from stack.
* **`GameServiceIntegrationTest`** — 3-test suite covering setup tie-break (SETTING_UP_ORDER), Shock Out chip transfer, and Phase 2 winner-to-loser redistribution.
* **`Player` entity fixed** — `@GeneratedValue(UUID)` added; `Player(String name)` constructor added to ensure correct JPA `persist()` lifecycle.
* All 23 tests passing.

## 5. Milestone 4: Game Flow Completion (Completed ✅)

### 4a. Virtual Roll Storage + Round Completion Guard *(Completed ✅)*
* `performVirtualRoll(UUID sessionId, UUID participantId)` added to `GameService`.
  Delegates to `DiceService.rollVirtually()`, derives `isHand` from `throwCount == 1`, persists via `gameParticipantRepository.save()`.
* **4a.1 — Null-Guard:** `evaluateRoundAndDistributeChips` throws `IllegalStateException`
  if any roller has `getLastRoll() == null`. *(Tech Debt resolved)*

### 4b. Round Reset + RoundResultDTO *(Completed ✅)*
* `rollers.forEach(GameParticipant::resetRoll)` called automatically after every round evaluation.
* `RoundResultDTO` record introduced as immutable snapshot: `loserParticipantId`, `loserName`,
  `chipsTransferred`, `allRolls` (captured **before** reset), `isGameOver`.
* `evaluateRoundAndDistributeChips` return type changed `void` → `RoundResultDTO`.

### 4c. Half-Time Loss Tracking *(Completed ✅)*
* `handlePhaseTransitions(GameSession, GameParticipant loser)` private method:
  * `FIRST_HALF` + `centralStack == 0` → `lostFirstHalf = true`, chips reset to 0, stack reset to 13, phase → `SECOND_HALF`.
  * `SECOND_HALF` + `centralStack == 0` → `lostSecondHalf = true`; if `hasLostMatch()` → `GAME_OVER`, else → `FINAL_MATCH`.
* Two `if`-blocks (not `else-if`) to handle ShockOut-on-full-stack edge case in a single transaction.

### 4d. FINAL_MATCH Logic + GAME_OVER Condition *(Completed ✅)*
* `evaluateRoundAndDistributeChips` guards against `GAME_OVER` at entry.
* Dedicated `FINAL_MATCH` branch: enforces exactly 2 rollers, runs normal chip distribution,
  sets `GAME_OVER` when `centralStack == 0`.
* `getOrderedFinalists(GameSession)` public method: returns participants with `lostFirstHalf || lostSecondHalf`;
  requires `FINAL_MATCH` or `GAME_OVER` phase.
* All 24 tests passing after implementation.

## 6. Milestone 4b: Architecture Hardening (Completed ✅)

* **`@OrderColumn(name = "seat_order")` on `GameSession.participants`** — Hibernate now writes
  a numeric index column to `game_participant`. Participant list order survives every DB reload.
  Without this, `participants.get(0)` was non-deterministic across transaction boundaries,
  silently breaking LIFO/FIFO evaluation.

* **`createSession` player load fixed** — `findAllById(playerIds)` replaced by a
  `stream().map(findById).toList()`. Preserves caller-supplied `playerIds` order.
  `findAllById` returns rows sorted by PK, not by input order.

* **E2E test strengthened** — `assertThat(participants.get(0).getId()).isEqualTo(aliceId)`
  reinstated as active proof that `reorderParticipantsStartingWith` persists correctly.
  All 24 tests passing.

## 7. Milestone 5: REST API Basics (Completed ✅) — 26 Tests grün

* **`GameController` live** — 4 POST-Endpoints:
  * `POST /api/sessions` → 201 + `GameSessionDTO`
  * `POST /api/sessions/{id}/setup` → evaluates setup rolls, returns tied participants
  * `POST /api/sessions/{id}/participants/{pid}/roll` → virtual roll, 200 + `ParticipantDTO`
  * `POST /api/sessions/{id}/evaluate` → round evaluation, 200 + `RoundResultDTO`
* **`ParticipantDTO` UI-ready** — enthält `throwCount` (max-3-throws-Enforcement im Frontend)
  und `safe` (visuelle Hervorhebung sicherer Spieler).
* **`GlobalExceptionHandler`** — wandelt `IllegalStateException`, `PlayerNotFoundException`
  und `EntityNotFoundException` in strukturierte JSON-Fehlerantworten um.
* **`GameControllerTest`** — `@WebMvcTest` mit `@MockitoBean` (kein deprecated `@MockBean`):
  verifiziert 201 + DTO-Struktur für `createSession` sowie `throwCount` und `safe`
  via `jsonPath` für `performRoll`. 26 Tests grün.

## 8. Milestone 6: Web Frontend — Thymeleaf Dashboard (Completed ✅)

* **Dependency:** `spring-boot-starter-thymeleaf` added to `build.gradle.kts`.
* **`ViewController.java`** — `@Controller` (not `@RestController`) at `GET /`:
  * Seeds Alice & Bob via `playerRepository.save()` for a stable demo state on every restart.
  * Creates a `GameSession` and passes both `session` and `participants` explicitly to the `Model`.
  * Returns `"index"` — Spring Boot maps this to `src/main/resources/templates/index.html`.
* **`templates/index.html`** — Dark-themed Bootstrap 5 monitoring dashboard:
  * Displays current `GamePhase`, `centralStack`, and `Session ID`.
  * Iterates over participants with `th:each`, shows roll, throw count, penalty chips, and safe/active status.
  * Empty-state fallback row via `${#lists.isEmpty(participants)}`.
  * "Reset & Create New Session" button reloads the page, triggering fresh seeding.
  * Live timestamp via Thymeleaf utility: `${#dates.format(#dates.createNow(), 'HH:mm:ss')}`.
* **Application running** at `http://localhost:8080/` — first full Backend-to-Browser data flow verified.

### Troubleshooting Lessons Learned
* **Gradle Sync:** Changes in `build.gradle.kts` require clicking the "Load Gradle Changes"
  icon (Blue Elephant in IntelliJ) to take effect. The IDE does not auto-detect them.
* **Template Folder Convention:** Spring Boot auto-discovers templates **only** in
  `src/main/resources/templates/` (lowercase). Any typo breaks the resolution silently.
* **Template Return Value:** The string returned from a `@Controller` method must match
  the filename exactly (without `.html`). `return "index"` → `templates/index.html`.
* **Model vs Entity:** Passing `session.getParticipants()` as a separate model attribute
  (`"participants"`) was necessary because Thymeleaf's `th:each` needs a direct list reference,
  not a nested property traversal through the session object.

## 9. Milestone 7: HTMX Digital Tavern (Completed ✅)

Architectural transition from a static Thymeleaf monitoring dashboard to an interactive
"Digital Tavern" with partial page updates and zero full-page reloads.

### 7a. Fragment Architecture
* `index.html` reduced to a **page shell** — includes two named fragments via `th:replace`.
* `templates/fragments/tavern-table.html` — dice area. `id="tavern-table"` on root element.
  Roll buttons use `hx-post="/game/roll"` + `hx-target="#tavern-table"` + `hx-swap="outerHTML"`.
* `templates/fragments/player-sidebar.html` — stats panel. Self-refreshes via
  `hx-trigger="diceRolled from:body"` + `hx-swap="outerHTML"` on the root element.

### 7b. ParticipantViewModel
* `ParticipantViewModel.java` introduced as a **view-layer record** — decouples templates
  from JPA entities. Contains only semantic state (`boolean safe`, `boolean canRoll`).
* No CSS strings in Java — `th:classappend` in the template decides visual styles.
* `avatarUrl` and `skinId` fields reserved as hooks for future avatar/theming feature.

### 7c. ViewController Refactored
* Fragment name constants (`FRAGMENT_TAVERN_TABLE`, `FRAGMENT_PLAYER_SIDEBAR`) as
  `private static final String` — single point of change if templates are renamed.
* `HttpSession` stores the active `gameSessionId` — all fragment endpoints resolve
  the session without requiring the client to track it.
* `POST /game/roll` → performs virtual roll, returns `FRAGMENT_TAVERN_TABLE`,
  sets `HX-Trigger: diceRolled` response header.
* `GET /game/table-fragment` and `GET /game/sidebar-fragment` — direct fragment endpoints.

### 7d. Sync Architecture: HX-Trigger over OOB
* Chose **`HX-Trigger: diceRolled`** over `hx-swap-oob` for the sidebar update.
  Reason: the sidebar self-refreshes as an independent observer — future events
  (e.g. `roundEvaluated`) require no changes to POST endpoints.

### 7e. JPA Entity Fix: `@Data` → `@Getter @Setter` on `GameSession`
* `@Data` is an anti-pattern on JPA entities — its generated `equals()/hashCode()` over
  all fields interferes with Hibernate's dirty checking.
* Replaced with explicit `@Getter @Setter`. All three public getters (`getId()`,
  `getPhase()`, `getCentralStack()`) are now guaranteed and visible to Thymeleaf.
* `ViewController.index()` re-fetches the session via `findById()` after `createSession()`
  to guarantee a clean, DB-backed entity (consistent with all other endpoints).

### 7f. Critical Bugfix: Thymeleaf Reserved Name Collision
* **Root cause:** model attribute key `"session"` collides with Thymeleaf's built-in
  `${session}` context variable, which points to the browser's `HttpSession` — not the
  `GameSession` entity. All scalar field accesses (`phase`, `centralStack`, `id`) silently
  resolved against `HttpSession`, returning null/empty. `participants` worked because it
  used a non-reserved name.
* **Fix:** renamed model attribute from `"session"` to `"game"` in `ViewController.populateModel()`.
  All template expressions updated from `${session.*}` to `${game.*}`.
* **Reserved Thymeleaf names to avoid as model keys:** `session`, `request`, `response`,
  `application`, `param`.

## 10. Milestone 8: REST API Completion (Planned ⏳)

### 8a. GET-Endpoints
* `GET /api/sessions/{id}` — query current session state.
* Required for frontend synchronisation between actions.

### 8b. Controller Error-Case Tests
* Error-path tests for `GameController`: wrong phase, missing rolls, unknown IDs —
  verified via `GlobalExceptionHandler` + `jsonPath("$.error")`.

## 11. Technical Debt & Notes
* *Note:* Ensure all future business logic remains in Services/Evaluators, not in Entities (SRP).
* *Note:* Maintain 100% English naming convention for all new components.
* *Note:* `@NoArgsConstructor(force = true)` on `DiceRoll` creates a JPA-only constructor with null dice — never call `new DiceRoll()` directly in production code.
* *Note:* `GameSession` now uses `@Getter @Setter` instead of `@Data`. Do NOT revert to `@Data` — this is an intentional architectural fix for JPA entity integrity.
