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

## Phase 3: Penalty Logic — IN BEARBEITUNG
*(Start: 23. März 2026)*

- Penalty Value Matrix konzeptionell entworfen und als technischer Vertrag festgelegt.
- Werte: Schock Aus = 13 (semantische Konstante), Schock = 3. Würfel, General = 3, Straße = 2, Hausnummer = 1.
- `DiceRoll.getPenaltyValue()` als Pflicht-Methode definiert — Implementierung ausstehend.
- Dokumentations-Automatisierung via "Execution Command" eingeführt.

**Nächste Schritte:**
- `getPenaltyValue()` implementieren.
- Strafstein-Verteilungslogik im Service.
- Anbindung an `GameSession` (Chip-Transfer).

Vollständiges Protokoll: `journals/2026/03_March/2026-03-23_protocol.md`
