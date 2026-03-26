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
