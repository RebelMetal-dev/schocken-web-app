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

* **Mindestens 1 Wurf:** Der Beginner muss mindestens einmal würfeln. Er entscheidet durch sein Verhalten (wann er aufhört) das Limit: **1×, 2× oder 3×**. Dies gilt verbindlich für alle Nachfolger.
* **Offen oder blind:** Deckt der Beginner nach seinem letzten Wurf auf → er hat **offen** gespielt. Lässt er den Becher liegen → er hat **blind** gespielt. Beides ist erlaubt.
* **Blind-Zwang:** Wer das Limit voll ausschöpft (z.B. den 3. Wurf macht), **muss** den Becher verdeckt lassen — Aufdecken löst sofort einen Strafchip aus.
* **Blind stehen lassen (Thrill):** Wer vor dem Limit aufhört, kann den Becher freiwillig verdeckt lassen (Bluff), um Nachfolger im Unklaren zu lassen.
* **Nachfolger dürfen weniger würfeln:** Ein Nachfolger darf auch weniger als das Limit würfeln und seinen Becher verdeckt lassen — erhöht den Thrill-Faktor.

#### Blind-Zwang für alle: die wichtigste Gruppenregel
Würfelt der Beginner **blind** (Becher bleibt verdeckt):
* **Alle Nachfolger müssen ebenfalls verdeckt bleiben** — kein Spieler darf vor dem Showdown aufdecken.
* Erst wenn **alle gewürfelt haben**, darf der Beginner als Erster aufdecken.
* Danach decken alle **links vom Beginner der Reihe nach** auf — um die Spannung zu halten.

Würfelt der Beginner **offen** (Becher aufgedeckt):
* Nachfolger dürfen frei entscheiden, wann sie aufdecken — solange sie nicht nochmals würfeln.

#### Strafdeckel (Strafchip-Auslöser)
| Verstoß | Strafe | Wertung |
| :--- | :--- | :--- |
| Zu viel gewürfelt (über Limit) | 1 Strafchip sofort | **Erster Wurf** zählt — spätere Würfe ungültig |
| Aufgedeckt obwohl Blind-Zwang gilt | 1 Strafchip sofort | Erster Wurf zählt |
| Aufgedeckt obwohl Beginner blind gespielt hat | 1 Strafchip sofort | Erster Wurf zählt |

> **Entwicklerhinweis:** Bei einem Strafauslöser muss `lastRoll` auf den **ersten** Wurf eingefroren
> werden — weitere `applyRoll()`-Aufrufe dürfen `lastRoll` dann nicht mehr überschreiben.
> MVP-TODO: aktuell überschreibt `applyRoll()` `lastRoll` bei jedem Wurf.

### 2b. Sonderregel: Hand-Wandlung (Sechsen-Wandlung)
* **Nur 1en sammeln:** Es dürfen ausschließlich Einsen (oder gewandelte Sechsen) rausgelegt werden.
* **Wandlung:** Nur möglich bei einem Wurf **"aus der Hand"** (alle aktuell genutzten Würfel gleichzeitig geworfen).
  * Zwei Sechsen (6,6) im Handwurf → eine Sechs wird zur Eins umgedreht und rausgelegt, die andere geht zurück in den Würfelpool. Verbleibende Würfel: **N − 1**.
  * Drei Sechsen (6,6,6) im Handwurf → zwei Sechsen werden zu Einsen umgedreht und rausgelegt, die dritte geht zurück in den Würfelpool. Verbleibende Würfel: **N − 2**.
* **Weiterwürfeln ist Pflicht:** Nach jeder Wandlung muss der Spieler mit den verbleibenden Würfeln sofort weiterwürfeln — Stehenbleiben nach der Wandlung ist nicht erlaubt.
* **Beispiele (Start: 3 Würfel):**
  * Wurf 6,6,3 → eine 6 wird zur banked 1 → Pool: die andere 6 + die 3 → **2 Würfel weiterwürfeln**
  * Wurf 6,6,6 → zwei 6en werden zu banked 1,1 → Pool: die dritte 6 → **1 Würfel weiterwürfeln**
  * Wurf 6,6,6 → Wandlung → 1 Würfel ergibt 1 → Bank: [1,1,1] = **Schock-Aus** (aber Blind-Zwang gilt wenn es der Limit-Wurf war)
* **Risiko-Reset:** Ein Spieler kann jederzeit alle rausgelegten Einsen wieder einpacken und alles neu werfen, um den **Hand-Status** zu erzwingen.

### 2c. Reihenfolge & Showdown
* Jede neue Runde beginnt der **Verlierer der Vorrunde**. Die Reihenfolge verschiebt sich dadurch ständig.
* **Sitzordnung:** Immer **links vom Beginner** (Uhrzeigersinn).

#### Showdown-Regeln
| Situation | Aufdeckungs-Reihenfolge |
| :--- | :--- |
| Beginner würfelt **1× und deckt sofort auf** (erster Wurf, offen) | Alle Nachfolger dürfen **frei** aufdecken wann sie wollen |
| Beginner würfelt **mehrmals** (2× oder 3×) | Sequentiell: **links vom Beginner** der Reihe nach |
| Beginner spielt **blind** (Becher bleibt verdeckt) | Beginner deckt als **Erster** auf, dann sequentiell links |

> **Entwicklerhinweis — Gleichzeitigkeit im Showdown:** In der Web-App klickt jeder einzeln auf
> "Aufdecken". Die Würfel werden für alle erst sichtbar, wenn **alle** `cupRevealed = true` gesetzt
> haben — dann ein einziger Broadcast. So bleibt die Spannung des simultanen Aufdeckens erhalten.

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