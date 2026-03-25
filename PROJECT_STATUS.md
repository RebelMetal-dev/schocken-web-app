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

## 10. Milestone 8: Resilience, REST API & HTMX Error Handling (Completed ✅) — 29 Tests grün

### 8a. Hybrid Error Handling via `@ControllerAdvice`
* **Root problem:** `@RestControllerAdvice` forces `@ResponseBody` on every handler method —
  making it impossible to return a Thymeleaf fragment. Replaced with `@ControllerAdvice`.
* **Branching logic:** `isHtmxRequest(HttpServletRequest)` checks the `HX-Request: true` header.
  * HTMX client → `ModelAndView("fragments/error-alert :: error-alert")` — fragment rendered by Thymeleaf.
  * REST client → `ResponseEntity<Map>` with JSON body and correct HTTP status (404 / 400).
* **Why `Object` return type:** Spring MVC dispatches based on actual runtime type —
  `ModelAndView` → Thymeleaf pipeline, `ResponseEntity` → `HttpEntityMethodProcessor` → Jackson.
* **`fragments/error-alert.html`** — Bootstrap dark-theme error card matching Tavern style.
  Displays `${errorMessage}` from the `ModelAndView`; includes a "Reset & Return to Tavern" link.

### 8b. `GET /api/sessions/{id}` Endpoint
* `GameService.getSession(UUID)` — `@Transactional(readOnly = true)` for minimal DB overhead;
  throws `EntityNotFoundException` if session not found.
* `GameController.getSession()` — maps to `GameSessionDTO`, returns 200 or is handled by
  `GlobalExceptionHandler` (404 JSON or error fragment depending on client type).

### 8c. Optimistic Locking on `GameSession`
* `@Version Long version` added to `GameSession` — Hibernate increments on every `UPDATE`.
* Concurrent transactions reading the same `version` → second commit throws `OptimisticLockException`,
  preventing lost updates on `centralStack`.
* Chosen over pessimistic locking: Schocken is turn-based; true concurrent writes are exceptional,
  not the norm. DB row locks would block all players unnecessarily.

### 8d. Critical Bugfix: `Stream.toList()` + `@Version` Interaction
* **Root cause:** `Stream.toList()` (Java 16+) returns an **unmodifiable** list.
  Adding `@Version` changed Hibernate's internal merge path to `entityIsPersistent →
  copyValues → replaceElements()`, which calls `collection.clear()` on the participants list.
  An unmodifiable list throws `UnsupportedOperationException` at that point.
* **Why it was hidden before:** Without `@Version`, Hibernate used a different merge path
  that never called `replaceElements`. The fragility existed in the code, but was never triggered.
* **Fix:** `session.setParticipants(new ArrayList<>(...))` in `createSession()` — wraps the
  unmodifiable stream result in a mutable `ArrayList` before Hibernate ever touches it.
* **Rule:** Any JPA-managed collection set via a service method must be a **mutable** list.
  Never assign `List.of(...)` or `Stream.toList()` directly to a JPA entity field.

### 8e. Test Coverage
* Three new `@WebMvcTest` cases in `GameControllerTest`:
  * `getSession_existingId` → 200 + DTO field assertions.
  * `getSession_unknownId_restClient` → 404 + `jsonPath("$.error").exists()`.
  * `getSession_unknownId_htmxClient` → 200 + `content().string(containsString("Tavern Error"))`.
* All 29 tests passing.

## 11. Milestone 9: Spielbarer Loop (Completed ✅) — 29 Tests grün

### 9a. Error-Target in der UI
* `index.html`: `<div id="error-target" class="mb-3"></div>` added above the main row as
  a dedicated swap zone for HTMX error fragments returned by `GlobalExceptionHandler`.

### 9b. Evaluate-Button + ViewController
* `POST /game/evaluate` in `ViewController` — resolves participant IDs server-side from
  `HttpSession`, calls `gameService.evaluateRoundAndDistributeChips()`, returns updated
  `FRAGMENT_TAVERN_TABLE`, sets `HX-Trigger: roundEvaluated` response header.
* `canEvaluate` semantic boolean added to `populateModel()` — `true` for phases
  `FIRST_HALF`, `SECOND_HALF`, `FINAL_MATCH`. Template decides button visibility — no phase
  strings in HTML.
* Round Result Banner in `tavern-table.html` — shows loser name, chips transferred, and
  game-over status when `roundResult != null`. Disappears automatically on next roll swap.
* `player-sidebar.html`: `hx-trigger` extended with `roundEvaluated from:body` — sidebar
  self-refreshes on both dice rolls and round evaluations without OOB coupling.
* `hx-swap="outerHTML"` confirmed as the correct swap strategy on all forms — Thymeleaf
  returns the full fragment wrapper div including `id="tavern-table"` when rendering named
  fragments. `innerHTML` caused double-nesting (confirmed via DevTools Inspector).

### 9c. GameParticipant `@Data` Fix
* `@Data` replaced with `@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)`
  + `@EqualsAndHashCode.Include` on `@Id UUID id`.
* Root cause: `@Data` generates `equals()/hashCode()` over all fields — when `lastRoll`
  changes from `null` to a `DiceRoll`, the object's hash changes mid-session, causing
  Hibernate dirty-check instability.
* Fix: ID-only equality — two participants are equal if and only if they share the same
  database identity (`UUID id`).

### 9d. HTMX Deep Debug: App confirmed working
* Symptom: clicking "Roll Dice" appeared to do nothing in the browser.
* Investigation via Firefox DevTools + `htmx.logAll()`:
  * `HX-Request: true` in request headers → HTMX intercepting correctly ✅
  * `Content-Type: text/html`, `HX-Trigger: diceRolled` in response headers ✅
  * Response body starts with `<div id="tavern-table">` → correct fragment returned ✅
  * Console log showed `htmx:beforeSwap` + `htmx:beforeCleanupElement` for every child
    element → HTMX WAS performing the outerHTML swap correctly ✅
  * Actual response data: Alice `6, 5, 4` (5 throws), Bob `6, 5, 1` (1 throw) → data
    persisting correctly, throwCount accumulating across multiple rolls ✅
* Root conclusion: the app was working all along. The dice display changes are visually
  subtle (small `font-monospace` span). Visual feedback enhancement planned as Milestone 10.

## 12. Technical Debt & Notes
* *Note:* Ensure all future business logic remains in Services/Evaluators, not in Entities (SRP).
* *Note:* Maintain 100% English naming convention for all new components.
* *Note:* `@NoArgsConstructor(force = true)` on `DiceRoll` creates a JPA-only constructor with null dice — never call `new DiceRoll()` directly in production code.
* *Note:* `GameSession` now uses `@Getter @Setter` instead of `@Data`. Do NOT revert to `@Data` — this is an intentional architectural fix for JPA entity integrity.
* *Note:* Any list assigned to a JPA-managed `@OneToMany` field must be a **mutable** `ArrayList`. `Stream.toList()` and `List.of()` return unmodifiable lists — Hibernate's `replaceElements()` will throw `UnsupportedOperationException` during merge.
