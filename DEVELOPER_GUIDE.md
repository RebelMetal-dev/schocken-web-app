# Schocken-Web-App: Developer Guide

---

## 1. Terminal Navigation (Git Bash)
- `STRG + L`: Bildschirm leeren
- `STRG + C`: Laufenden Befehl abbrechen
- `taskkill /F /IM claude.exe`: Claude-Prozess via CMD beenden

---

## 2. Standard Git-Workflow
- `git status`: Status prüfen
- `git add .`: Alle Änderungen vormerken
- `git commit -F msg.txt`: Commit mit Nachricht aus Datei
- `git push -u origin main`: Auf GitHub sichern

---

## 3. History Management (Anonymisierung & Korrektur)
- `git reset --hard HEAD~1`: Den letzten Commit spurlos löschen (Vorsicht!)
- `git push --force-with-lease origin main`: Geänderte Historie sicher hochladen
- `git commit --amend`: Den letzten lokalen Commit nachträglich bearbeiten

---

## 4. Technical Specs: Wurfsystem

### Datenmodell

```
GameParticipant
├── throwCount           (int)     — Gesamtanzahl Würfe der Runde
└── @Embedded DiceRoll
    ├── roll_throw_count (int)     — Wurf-Nr., in dem das Ergebnis erzielt wurde
    ├── roll_is_hand     (boolean) — true wenn Ergebnis im ersten Wurf erzielt
    └── dice[]           (int[3])  — die drei Würfelwerte
```

### Naming Convention

| Java-Feld | DB-Spalte | Bedeutung |
| :--- | :--- | :--- |
| `DiceRoll.throwCount` | `roll_throw_count` | Wurf-Nummer des Ergebnisses (1 = Hand) |
| `DiceRoll.hand` | `roll_is_hand` | `true` wenn `throwCount == 1` |
| `GameParticipant.throwCount` | `throw_count` | Gesamtwürfe der Runde |

### Ranking-Gesetze (Priorität absteigend)

1. **Schock-Rang** — `1-1-1` (höchster) bis `1-1-6` (niedrigster Schock)
2. **Hand-Bonus** — Gleiches Ergebnis: `throwCount == 1` schlägt `throwCount > 1`
3. **LIFO-Tie-Break** — Absoluter Gleichstand: der zeitlich letzte Spieler verliert

### Penalty Value Matrix

| Würfelkombination | Bedingung | Strafwert |
| :--- | :--- | :--- |
| `1-1-1` (Schock Aus) | Alle drei Würfel zeigen 1 | **13** ¹ |
| `1-1-x` (Schock) | x ∈ {2, 3, 4, 5, 6} | Wert des 3. Würfels (z. B. `1-1-5` → `5`) |
| General | Drei gleiche Würfel, kein Schock | `3` |
| Straße | Drei aufeinanderfolgende Werte | `2` |
| Hausnummer | Kein Sonderfall | `1` |

> ¹ **Semantische Konstante:** Der Wert `13` ist keine willkürliche Magic Number. Er ist direkt an die physische Steinanzahl im Schocken-Spiel gekoppelt und dient als Marker für "Halbzeit-Ende / Alles nehmen". Der Service erkennt diesen Sonderfall über `isShockOut()`, nicht über den Zahlenwert.

### DiceRoll — Implementierte Methoden

```java
public int getPenaltyValue()  // Strafsteine basierend auf RollType (exhaustiver switch)
public boolean isShockOut()   // true wenn RollType == SHOCK_OUT (Halbzeit-Trigger)
public RollType getType()     // Kategorisiert den Wurf: SHOCK_OUT, SHOCK, TRIPLET, STRAIGHT, HOUSE_NUMBER
public int compareTo(DiceRoll other) // Ranking: Typ > Hand > ThrowCount > Würfelwerte
```

---

## 5. Technical Specs: GameService — Chip-Verteilungslogik

### `evaluateRoundAndDistributeChips` — Ablauf

```
1. Session laden (einmalig)
2. p1, p2 direkt aus session.getParticipants() auflösen
3. RoundEvaluator.findLoser(List.of(p1, p2)) → loser bestimmen
4. winner = (loser == p1) ? p2 : p1
5. penalty = winner.getLastRoll().getPenaltyValue()
6. if (winner.getLastRoll().isShockOut())
       → handleShockOut: Loser nimmt alle verbleibenden Chips aus dem Stack
       → session.setPhase(SECOND_HALF)
   else
       → distributeChips(session, winner, loser, penalty)
       → if (session.getCentralStack() == 0) session.setPhase(SECOND_HALF)
7. gameSessionRepository.save(session)  ← Cascade sichert Participants
```

### Chip-Verteilung: Phase 1 → Phase 2

```
Phase 1 — Zentraler Stapel:
  fromStack = min(centralStack, penalty)
  centralStack -= fromStack
  loser.penaltyChips += fromStack

Phase 2 — Spieler-zu-Spieler (wenn Stack leer und Fehlbetrag > 0):
  stolen = min(winner.penaltyChips, remaining)
  winner.penaltyChips -= stolen
  loser.penaltyChips += stolen

Edge Case: Weder Stack noch Winner haben genug Chips
  → log.warn (kein Silent-Fail)
```

### Invarianten

- Session wird **einmal** geladen — Participants werden direkt aus der geladenen Session aufgelöst (kein redundanter DB-Call).
- Winner/Loser werden **intern** vom Service via `RoundEvaluator` bestimmt — der Controller übergibt nur Participant-IDs, nie Rollen.
- `@Transactional` auf allen öffentlichen Methoden — Cascade-Save für Participants via Session-Save.
