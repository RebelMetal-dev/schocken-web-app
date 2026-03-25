# Schocken — Vollständige Spielregeln (Referenz für Implementierung)

Quelle: Direktes Diktat vom Spieleigentümer. Massgeblich für alle UI- und Service-Entscheidungen.

---

## 1. Anfangsrunde (Setup Phase)

* Alle Mitspieler würfeln **genau einmal** mit allen 3 Würfeln im Schockbecher.
* Alle heben **gleichzeitig** auf — kein Spieler darf vorher schauen.
* Die Ergebnisse werden ausgewertet: der **Verlierer** der Anfangsrunde bekommt die Deckel
  (Chips) lt. Wertigkeit des Gewinnerwurfes direkt aus dem CentralStack.
* **Stechen (Tie-Break in der Anfangsrunde):**
  * Wenn 2+ Spieler denselben **besten** Wurf haben → diese stechen.
  * Wenn 2+ Spieler denselben **schlechtesten** Wurf haben → diese stechen.
  * Beim Stechen in der Anfangsrunde spielt die Reihenfolge keine Rolle
    (alle würfeln ja sowieso nur einmal).
  * Nach dem Stechen: der Verlierer des Stechens bekommt die Chips lt. Wertigkeit des
    Gewinnerwurfes aus dem Stechen.
* Der **Verlierer der Anfangsrunde** ist der **Beginner** — er darf als erster in der
  eigentlichen Partie anfangen.
* Die **Sitzordnung** ergibt sich automatisch: der Spieler links vom Beginner ist der
  Nächste, dann der übernächste usw.

---

## 2. Hauptrunde — Ablauf

### 2a. Wurf-Limit (Beginner-Regel)
* Der **Beginner entscheidet** zu Beginn seiner Runde, wie oft er würfelt: **1×, 2× oder 3×**.
* Alle anderen Spieler dürfen **maximal gleich oft** würfeln wie der Beginner.
* Würfelt der Beginner nur 1×, dürfen alle nur 1× würfeln — auch wenn sie möchten.

### 2b. Reihenfolge & Aufdecken
* Jeder Spieler würfelt der Reihe nach (gemäss Sitzordnung, links vom Beginner weiter).
* **Frühes Aufdecken verboten:** Kein Spieler darf seinen Deckel hochheben und die Würfel
  anschauen, bevor er dran ist. Wer zu früh aufdeckt → **Strafchips**.
* Der Beginner legt nach seinem letzten Wurf seinen Becher offen — alle anderen sehen
  erst dann das Ergebnis des Beginners.

### 2c. Sonderregel: Hand-Kombination (Schock greifen)
* Wenn ein Spieler **aus der Hand** (erster Wurf) zwei Sechsen hat, kann er diese
  beiden Sechsen zu **einer Eins** zusammenlegen.
* Der eine "kombinierte" Würfel zeigt dann eine 1. Mit den verbleibenden Würfeln
  würfelt er weiter und versucht noch eine oder zwei weitere Einsen zu bekommen.
* Ziel: Schock (x, 1, 1) oder Schock-Aus (1, 1, 1) erreichen.

### 2d. Rundenauswertung
* Wenn alle Spieler gewürfelt haben, werden die Ergebnisse verglichen.
* Der **Verlierer** bekommt die Deckel lt. Wertigkeit des **Gewinnerwurfes**.
* **Chip-Quelle:** Chips kommen immer zuerst aus dem CentralStack.
  Erst wenn der CentralStack leer ist, werden Chips von Spieler zu Spieler verteilt.
* **Tie-Breaking bei gleicher Wertigkeit:**
  1. **Hand-Wurf schlägt kombinierten Wurf** — wer seinen Wurf in einem Zug (aus der Hand)
     erreicht hat, gewinnt gegenüber demjenigen der mehrfach würfeln musste.
  2. **"Mit ist shit" (Reihenfolge-Regel):** Bei gleicher Wertigkeit und gleicher Art
     (beide Hand oder beide kombiniert) gewinnt der Spieler der **früher** in der Runde
     gewürfelt hat — wer zuerst kommt, mahlt zuerst.

---

## 3. Wertigkeit der Würfe (Rangfolge — höchster zuerst)

| Rang | Bezeichnung | Beispiel | Penalty-Chips |
| :--- | :--- | :--- | :--- |
| 1 (höchste) | Schock-Aus | 1, 1, 1 | 13 (gesamter Stack) |
| 2 | Schock x | x, 1, 1 (x = Augenzahl) | x Chips |
| 3 | Triplet | x, x, x | 3 Chips |
| 4 | Strasse | x, x-1, x-2 | 2 Chips |
| 5 (niedrigste) | Hausnummer | alle anderen | 1 Chip |

*Innerhalb eines Rangs: höherer Zahlenwert gewinnt (z.B. Schock 6 > Schock 5).*

---

## 4. Erste & Zweite Hälfte

* Eine Hälfte läuft so lange bis **ein Spieler genau 13 Chips** angesammelt hat.
* Dieser Spieler hat die Hälfte **verloren** — er bekommt einen roten Markierungsdeckel
  (oder ein anderes Objekt) als Kennzeichnung.
* Der **Verlierer der ersten Hälfte** ist der **Beginner für die zweite Hälfte** —
  er entscheidet wieder das Wurf-Limit.
* Die zweite Hälfte läuft identisch zur ersten.

---

## 5. Spielende & Finale

### 5a. Blattschuss
* Wenn der **gleiche Spieler** beide Hälften verloren hat → **Blattschuss**.
* Das Spiel ist sofort vorbei. Dieser Spieler ist der **Gesamtverlierer**.

### 5b. Finale (2 verschiedene Halbzeit-Verlierer)
* Wenn **zwei verschiedene Spieler** die Hälften verloren haben, spielen **nur diese zwei**
  das Finale. Alle anderen Spieler scheiden aus.
* Das Finale läuft wie eine normale Runde — bis einer der beiden 13 Chips hat.
* Dieser Spieler ist der **Gesamtverlierer**.

### 5c. Sonderregel: Versehentliches Einwürfeln ins Finale
* Falls ein Spieler, der eigentlich **nicht** im Finale ist, unachtsam einen Chip bekommt
  (z.B. weil er noch mitgewürfelt hat obwohl er nicht hätte sollen), muss er
  **mindestens diesen Wurf mitspielen**.
* Bekommt er dabei einen weiteren Chip → ist er offiziell in der Finalrunde dabei
  und kann als **dritter Spieler** der Gesamtverlierer werden.

---

## 6. Implementierungs-Backlog (offene Punkte)

Folgende Regeln sind in der **Business-Logik (Service-Layer) bereits implementiert**:
- Würfelbewertung und Rangfolge (`DiceRoll.compareTo()`)
- Chip-Verteilung aus CentralStack und Spieler-zu-Spieler (`distributeChips`)
- Setup-Phase Auswertung mit Stechen (`evaluateSetupAndDetermineOrder`)
- Phasenübergänge H1 → H2 → FINAL_MATCH → GAME_OVER (`handlePhaseTransitions`)
- Blattschuss-Erkennung (`hasLostMatch()`)
- Schock-Aus Sonderbehandlung (`handleShockOut`)

Folgende Punkte fehlen noch im **UI-Layer / Controller**:
1. **Beginner legt Wurf-Limit fest** (1/2/3) — alle anderen sind daran gebunden
2. **Reihenfolge-Anzeige** — wer ist gerade dran (aktiver Spieler hervorheben)
3. **Anfangsrunde als eigener UI-Flow** — alle würfeln einmal, dann gemeinsames Evaluate
4. **Hand-Würfel Sonderregel** — zwei Sechsen zu einer Eins kombinieren (UI + Service)
5. **Frühes-Aufdecken-Strafe** — in der digitalen Version vereinfacht (nicht dringend)
6. **Würfel-Visualisierung** — Würfelwerte als individuelle grosse Boxen statt String
7. **Markierungsdeckel** — visueller Hinweis auf Halbzeit-Verlierer (roter Deckel)
