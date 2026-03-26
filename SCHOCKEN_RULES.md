# Schocken — Vollständige Spielregeln v2 (Referenz für Implementierung)

Diese Datei enthält das verbindliche Regelwerk inklusive aller taktischen Feinheiten (Hand-Mutation, Goldene Mitte, Gefangen-Regel). Sie dient als Master-Logik für den `RoundEvaluator` und den `GameService`.

---

## 1. Anfangsrunde (Setup Phase)

* **Der Wurf:** Alle Mitspieler würfeln **genau einmal** mit allen 3 Würfeln verdeckt im Becher.
* **Simultanes Aufdecken:** Alle heben **gleichzeitig** auf — kein Spieler darf vorher schauen.
* **Bestimmung der Deckel:** Der höchste Wurf am Tisch bestimmt die Anzahl der Chips, die aus dem **CentralStack** (13 Stück) verteilt werden.
* **Stechen (Tie-Break in der Anfangsrunde):** * Wenn 2+ Spieler denselben **schlechtesten** Wurf haben → diese stechen, bis ein eindeutiger Verlierer feststeht.
* **Beginner:** Der finale Verlierer der Anfangsrunde ist der **Vorleger** der ersten Hauptrunde.
* **Sitzordnung:** Ergibt sich automatisch; der Spieler links vom Beginner ist der Nächste (Uhrzeigersinn).

---

## 2. Hauptrunde — Ablauf & Taktik

### 2a. Wurf-Limit & Blind-Option
* **Limit:** Der Beginner entscheidet, wie oft er würfelt: **1×, 2× oder 3×**. Dies ist das Limit für alle Nachfolger.
* **Taktik des Vorlegers:** Er kann nach dem 1. oder 2. Wurf **aufdecken**, um Druck aufzubauen, oder **blind liegen lassen** (Bluff).
* **Blind-Zwang:** Wer das Limit voll ausschöpft (z.B. den 3. Wurf macht), **muss** den Becher verdeckt lassen.
* **Blind stehen lassen (Thrill):** Wer vor dem Limit aufhört, kann den Becher dennoch verdeckt lassen, um Nachfolger im Unklaren zu lassen.
* **Strafchips:** Wer unaufgefordert zu früh aufdeckt (beim Limit-Wurf) oder einen Becher hebt, der laut Vorleger blind bleiben muss → **1 Strafchip sofort**.

### 2b. Sonderregel: Hand-Wandlung (Sechsen-Wandlung)
* **Nur 1en sammeln:** Es dürfen ausschließlich Einsen (oder gewandelte Sechsen) rausgelegt werden.
* **Wandlung:** Nur möglich bei einem Wurf **"aus der Hand"** (alle aktuell genutzten Würfel gleichzeitig geworfen).
  * Zwei Sechsen (6,6) im Handwurf → können zu **einer Eins** umgedreht werden.
  * Drei Sechsen (6,6,6) im Handwurf → können zu **zwei Einsen** umgedreht werden.
* **Risiko-Reset:** Ein Spieler kann jederzeit alle bereits rausgelegten Einsen wieder einpacken und alles neu werfen, um den **Hand-Status** zu erzwingen.

### 2c. Reihenfolge & Showdown
* Jede neue Runde beginnt der **Verlierer der Vorrunde**. Die Reihenfolge verschiebt sich dadurch ständig.
* **Sequentieller Showdown:** Nach dem letzten Spieler werden alle verdeckten Becher **nacheinander per Interaktion** in Spielreihenfolge aufgedeckt.

---

## 3. Wertigkeit der Würfe (Rangfolge — höchster zuerst)

| Rang | Bezeichnung | Beispiel | Penalty-Chips |
| :--- | :--- | :--- | :--- |
| 1 (höchste) | Schock-Aus | 1, 1, 1 | 13 (gesamter Stack) |
| 2 | Schock x | x, 1, 1 (x = Augenzahl) | x Chips |
| 3 | Triplet (General) | x, x, x | 3 Chips |
| 4 | Strasse | x, x-1, x-2 | 2 Chips |
| 5 (niedrigste) | Hausnummer | alle anderen | 1 Chip |

---

## 4. Die "Schock-Dynamik" (Tie-Breaking & Wertung)

Bei gleicher Augenzahl (z.B. zwei Spieler haben einen Schock 4) entscheidet:

1. **Status-Regel:** **Hand-Wurf schlägt kombinierten Wurf.** Wer seinen Wurf in einem Zug (ohne Rauslegen) erreicht hat, gewinnt gegenüber demjenigen, der gesammelt hat.
2. **"Mit ist shit" (Reihenfolge):** Bei gleichem Status gewinnt der Spieler, der **früher** in der Runde gewürfelt hat. Der Nachzügler verliert.
3. **Die Goldene Mitte:** Ein Hand-Wurf kann einen Spieler zum Gewinner mutieren lassen, wodurch der ursprüngliche Gewinner (zusammen) in die "Goldene Mitte" rutscht und gerettet ist. Der schlechtere Wurf bleibt Verlierer.

---

## 5. Erste & Zweite Hälfte

* Wer **13 Chips** angesammelt hat, verliert die Hälfte → **Roter Markierungsdeckel**.
* Der **Verlierer der ersten Hälfte** ist der Beginner für die zweite Hälfte.
* **Blattschuss:** Wenn derselbe Spieler beide Hälften verliert → Spielende, dieser Spieler ist der Gesamtverlierer.

---

## 6. Finale & Sonderregeln

### 6a. Das Finale
* Zwei Hälften-Verlierer spielen gegeneinander, bis einer 13 Chips hat.

### 6b. Versehentliches Einwürfeln (Gefangen-Regel)
* Wer sich unachtsam ins Finale einwürfelt (Interaktion ausführt) und einen Chip kassiert, ist **"gefangen"**.
* Er muss so lange mitspielen, bis er seine Chips wieder los ist oder der CentralStack leer ist.

### 6c. Die Bierrunde (Tradition)
* Der finale Gesamtverlierer übernimmt die **Bierrunde** für alle Teilnehmer der Partie.