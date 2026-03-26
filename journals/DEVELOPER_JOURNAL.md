# Developer Journal: Schocken Web App

---

## Phase 1: Foundation — ABGESCHLOSSEN
*(vor März 2026)*

- Projekt-Infrastruktur steht (IntelliJ, Git, GitHub).
- `.gitignore` konfiguriert (Sicherheit für `.env` und lokale Claude-Settings).
- Git-Historie erfolgreich bereinigt und anonymisiert.
- Erfolgreicher "Clean Slate": Repository ist bereit für die erste Logik-Phase.

Vollständiges Protokoll: `journals/2026/03_March/2026-03-20_protocol.md`

---

## Phase 2: Refactoring des Wurfsystems & JPA Mapping-Fix — ABGESCHLOSSEN
*(20. März 2026)*

- `DiceRoll` als `@Embeddable` in `GameParticipant` eingebettet.
- JPA-Startupfehler (`Column 'throw_count' is duplicated`) via `@AttributeOverride` behoben.
- Ranking-Logik implementiert: Schock-Rang > Hand-Bonus > LIFO-Tie-Break.
- `DiceService`: `isHand` und `throwCount` explizit in `rollVirtually()` und `rollManually()` gesetzt.
- `RoundEvaluatorTest` — alle Tests GRÜN.

Vollständiges Protokoll: `journals/2026/03_March/2026-03-20_protocol.md`

---

## Phase 3: Penalty Logic & Service Layer — ABGESCHLOSSEN
*(23. März 2026)*

### Model Layer — ABGESCHLOSSEN

- `DiceRoll.getPenaltyValue()` implementiert (Penalty Value Matrix: Schock Aus = 13, Schock = 3. Würfel, General = 3, Straße = 2, Hausnummer = 1).
- `DiceRoll.isShockOut()` hinzugefügt — exponiert Halbzeit-Trigger ohne die Konstante 13 zu leaken.
- `DiceRollTest` auf 10 Cases erweitert (inkl. `6-6-6 → 3`, `1-2-3 → 2`).

### Service Layer — ABGESCHLOSSEN

- `RoundEvaluator` als Strategy-Komponente implementiert:
  - `findLoser(List<GameParticipant>)` — LIFO-Tie-Break: späterer Roller verliert bei Gleichstand.
  - `findWinner(List<GameParticipant>)` — FIFO-Tie-Break: früherer Roller gewinnt bei Gleichstand.
- `GameService.evaluateRoundAndDistributeChips` auf `List<UUID>` umgestellt (Multiplayer-fähig):
  - **Architektur-Entscheidung:** Umstieg von zwei fixen IDs auf flexible Liste. Controller übergibt nur Reihenfolge — der Service entscheidet Gewinner/Verlierer intern.
  - **Invariante:** Die Reihenfolge der IDs = Reihenfolge der Würfe. Diese Reihenfolge ist die alleinige Basis für den LIFO/FIFO-Tie-Break.
  - Participants werden via `.map(id -> ...)` in exakter ID-Reihenfolge aufgelöst — nicht via `filter()` auf der Session-Liste.
  - Phase 1: Chips zuerst aus dem zentralen Stapel.
  - Phase 2: Fehlbetrag wird vom Winner abgezogen.
  - SHOCK_OUT und leerer Stack lösen automatisch `GamePhase.SECOND_HALF` aus.
- `GameServiceIT` erstellt — Integrationstest mit 4 Spielern (Shock 2 gewinnt, Hausnummer verliert, 2 Chips aus Stack).

**Status:** Abgeschlossen — alle Tests GRÜN.

### Test-Debugging & JPA-Persistenz-Fix — ABGESCHLOSSEN
*(24. März 2026)*

- `GameServiceIntegrationTest` entwickelt und 5 Bugs behoben:
  - Compile-Error: fehlendes `participantIds`-Argument in `evaluateSetupAndDetermineOrder()`
  - Invertiertes Tie-Break-Szenario: die SCHLECHTESTEN Würfe müssen gleich sein, nicht die besten
  - Zu breite Null-Assertion: nur Tie-Kandidaten erhalten `null`-Roll, nicht alle Participants
  - Falscher Loser: `6-5-4` ist eine Straße, keine Hausnummer (`6-5-3` = echte Hausnummer)
  - Falscher Loser: `2-2-1` (HOUSE_NUMBER) ist schlechter als `6-5-4` (STRAIGHT)
- `GameServiceIT` repariert: `new Player(UUID.randomUUID(), ...)` → `new Player("Alice")` — JPA muss die ID generieren, nicht der Aufrufer
- `Player`-Entity: `@GeneratedValue(UUID)` + `Player(String name)` — korrekte JPA `persist()`-Lifecycle
- **Erkenntnisse:** Data Inconsistency (RAM vs. DB: `repository.save()` vor Service-Aufruf zwingend), Silent NPE im `RoundEvaluator` (kein null-Guard für `getLastRoll()` — Tech Debt für Phase 4)
- Alle 23 Tests GRÜN.

Vollständiges Protokoll: `journals/2026/03_March/2026-03-24_protocol.md`

---

### Setup-Phase & Persistenz-Fix — ABGESCHLOSSEN

- `IntegerListConverter` als `@AttributeConverter` implementiert: `List<Integer>` (Würfelwerte) wird als CSV-String in der DB gespeichert — JPA-Pflicht für Listentypen.
- `evaluateSetupAndDetermineOrder()` im GameService: Ermittelt den Startspieler vor der ersten Halbzeit.
  - Einzelner Verlierer → Participants rotieren, Phase wechselt auf `FIRST_HALF`.
  - Gleichstand → Rollen der Tied-Verlierer werden gelöscht, Phase wechselt auf `SETTING_UP_ORDER` (Stechen).
- `findAllLowestRollers()` zum `RoundEvaluator` hinzugefügt — liefert alle Spieler mit identisch schlechtestem Wurf.
- Guard in `evaluateSetupAndDetermineOrder()`: Nur in Phase `WAITING_FOR_PLAYERS` oder `SETTING_UP_ORDER` erlaubt.
- `calculatePenalty()` auf Delegation refactored: entfernt duplizierte `switch`-Logik, delegiert jetzt ausschließlich an `DiceRoll.getPenaltyValue()` (DRY).

Vollständiges Protokoll: `journals/2026/03_March/2026-03-23_protocol.md`

---

## Phase 4: API & Web Interface — GEPLANT

**Nächste Schritte:**
- REST Controllers für Session-Management und Roll-Endpoints.
- DTO-Mapping (interne Entities nicht direkt nach außen leaken).
- Globales Exception-Handling für Regelver­stöße.
