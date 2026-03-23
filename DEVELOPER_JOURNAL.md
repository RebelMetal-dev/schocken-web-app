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

Vollständiges Protokoll: `journals/2026/03_March/2026-03-23_protocol.md`

---

## Phase 4: API & Web Interface — GEPLANT

**Nächste Schritte:**
- REST Controllers für Session-Management und Roll-Endpoints.
- DTO-Mapping (interne Entities nicht direkt nach außen leaken).
- Globales Exception-Handling für Regelver­stöße.
