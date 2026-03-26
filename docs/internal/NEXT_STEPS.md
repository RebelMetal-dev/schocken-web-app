---
# Nächste Arbeitsschritte (Claude / KI-Arbeitsanweisungen)

Extrahiert aus VISION_AND_ROADMAP.md — internes Dokument, nicht für das öffentliche Portfolio.

---

## Phase 1 — Konkrete Implementierungsaufgaben

1. **DiceRoll.java:** Korrigiere die `compareTo`-Methode gemäß Prioritätskette:
   - Rang → Augenzahl → Hand-Status → Position ("Mit ist Shit")
   - Stichwort: Goldene Mitte — Hand schlägt Combined nur bei exakt gleicher Rang+Augenzahl-Kombination.

2. **GameService.java:** Abstraktion der Wurf-Quelle:
   - Private Hilfsmethode `applyRoll(participant, diceRoll)` extrahieren.
   - `performVirtualRoll` und `performManualRoll` delegieren dorthin.
   - Validierung: `throwCount` gegen `rollLimit` prüfen.

3. **GameSession / GameParticipant:** Zustandsvariablen ergänzen:
   - `GameSession.rollLimit` (int, 1–3) — vom Beginner gesetzt.
   - `GameSession.activeParticipantIndex` (int) — wer ist gerade dran.
   - `GameParticipant.cupRevealed` (boolean) — Becher physisch aufgedeckt (für sequentiellen Showdown).
   - Hinweis: `blind` existiert bereits auf `GameParticipant` — NICHT auf `GameSession` duplizieren.

---

## Offene Punkte / Technische Schulden

### Massen-Reveal am Showdown-Ende
- `revealCup()` im GameService ist die Einzelaktion (ein Spieler deckt auf, ggf. mit Strafchip).
- Am Ende einer Runde (Showdown) müssen **alle** `cupRevealed`-Flags auf `true` gesetzt werden,
  damit Frontend-Animationen die Würfel aller Spieler gleichzeitig anzeigen können.
- Verantwortung: `RoundEvaluator` oder als letzter Schritt in `evaluateRoundAndDistributeChips()`
  — vor dem `resetRoll()`-Aufruf.

### Verlierer als neuer Beginner
- `finishTurn()` zählt `activeParticipantIndex` mechanisch per Modulo hoch.
- Nach `evaluateRoundAndDistributeChips()` muss `activeParticipantIndex` auf den **Verlierer**
  gesetzt werden (dieser ist der Beginner der nächsten Runde, SCHOCKEN_RULES.md §2c).
- Aktuell fehlt dieser Schritt — zu klären mit Gemini ob das in `evaluateRound...()` oder
  in einer separaten `startNextRound()`-Methode passiert.
