# Schocken — Vollständige Spielregeln v2 (Referenz für Implementierung)

Diese Datei enthält das verbindliche Regelwerk inklusive aller taktischen Feinheiten (Hand-Mutation, Goldene Mitte, Gefangen-Regel). Sie dient als Master-Logik für den `RoundEvaluator` und den `GameService`.

---

## 1. Anfangsrunde (Setup Phase)

### Zweck
Die Anfangsrunde bestimmt:
1. Wer als **Beginner** (Vorleger) die erste Hauptrunde eröffnet
2. Wie viele **Chips** (Deckel/Pappen) dieser Spieler zu Beginn aus dem **Stock** erhält

### Ablauf
* Alle würfeln **genau einmal** mit allen 3 Würfeln verdeckt im Becher.
* Alle decken **gleichzeitig** auf — kein Spieler darf vorher schauen.
* Der Spieler mit dem **höchsten** Wurf gewinnt.
* Der Spieler mit dem **schlechtesten** Wurf verliert → wird Beginner der ersten Hauptrunde.
* Der Verlierer erhält aus dem Stock so viele Chips, wie der **Penalty-Wert des Gewinnerwurfs** beträgt.

| Gewinnerwurf | Beispiel | Chips für den Verlierer |
| :--- | :--- | :--- |
| Schock-Aus | 1,1,1 | 13 (gesamter Stock) |
| Schock x | 1,1,6 | 6 |
| Triplet | 3,3,3 | 3 |
| Straße | 4,5,6 | 2 |
| Hausnummer | 2,4,6 | 1 |

> **Entwicklerhinweis — Gleichzeitigkeit:** Im echten Spiel decken alle gleichzeitig auf.
> In der Web-App passiert das technisch nacheinander (jeder klickt "Aufdecken").
> Die Würfel aller Spieler werden erst dann für alle sichtbar, wenn der **letzte Spieler**
> auf "Aufdecken" geklickt hat — so bleibt die Spannung erhalten.
> Implementierung: `cupRevealed`-Flags sammeln → erst wenn alle `true` sind, sendet der
> Server einen Broadcast mit allen aufgedeckten Würfeln gleichzeitig.

### Stechen (Tie-Break)
* Haben 2+ Spieler **denselben schlechtesten** Wurf → nur diese stechen erneut.
* Alle anderen Spieler schauen zu.
* Wiederholt sich mit denselben Regeln bis ein **eindeutiger** Verlierer feststeht.
* Dieselben Rang-Regeln gelten wie in der Hauptrunde.

### Sonderfall: Sofortiger Halbzeit-Verlust
* Erhält der Verlierer alle 13 Chips (Gewinner würfelt Schock-Aus) → **erste Hälfte sofort verloren**.
* Er erhält einen **roten Markierungsdeckel** als Kennzeichnung.
* Derselbe Spieler beginnt die zweite Hälfte als Vorleger — mit den üblichen Regeln (1–3 Würfe, Blind-Option etc.).

### Einschränkungen der Anfangsrunde
* **Kein Würfel-Limit, keine Hand-Wandlung** — jeder würfelt genau einmal mit allen 3 Würfeln.
* Die Anfangsrunde ist technisch eine Hauptrunde mit festem Limit = 1 und deaktivierter Wandlung.

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