# 🎲 Schocken-Web-App: Vision & Technical Roadmap v2

## 1. Strategisches Ziel
Entwicklung eines hybriden Schocken-Ökosystems, das als digitaler Schiedsrichter fungiert. Die App soll sowohl rein virtuelles Spielen als auch haptisches Spielen (physische Würfel) unterstützen und regeltechnisch absichern.

---

## 2. Die Drei Betriebsmodi (ABC-Konzept)

*   **Modus A (Virtual-Only):** Der Server (`DiceService`) generiert Zufallszahlen.
*   **Modus B (Schiri-Modus):** Spieler würfeln haptisch mit echten Bechern. Die App dient zur Dokumentation. Ergebnisse werden manuell per UI eingetippt.
*   **Modus C (IoT/Remote):** Anbindung von Smart-Dice (Bluetooth) oder Remote-Schnittstellen (Discord), um physisches Würfeln global zu vernetzen.

---

## 3. Logik-Kern (Anpassung an SCHOCKEN_RULESv2.md)

### 3.1 Die "Goldene Mitte" (Mutation)
Die Bewertung eines Wurfs muss dynamisch erfolgen. Ein Hand-Wurf schlägt einen kombinierten Wurf nur bei exakt gleicher Augenzahl.
**Prioritätskette für den Vergleich:**
1. **Rang:** (Schock-Aus > Schock > General > Straße > Hausnummer)
2. **Augenzahl:** (Numerischer Wert, z.B. Schock 4 > Schock 2)
3. **Hand-Status:** (Hand-Wurf schlägt "Zusammen" bei gleichem Rang & Wert)
4. **Position:** ("Mit ist Shit" – der zeitlich frühere Wurf gewinnt bei absolutem Gleichstand)

### 3.2 Thrill-Faktor & Straf-Logik
*   **Manual Reveal:** Kein automatisches Aufdecken. Der Spieler muss aktiv "Lupfen" (Reveal-Button).
*   **Rule Enforcement:** Das System prüft gegen den Status des Vorlegers. Wer gegen die "Blind"-Ansage oder das Wurf-Limit aufdeckt, triggert automatisch: `penaltyService.addPenaltyChip(playerId, 1)`.

### 3.3 Die Gefangen-Regel
Die Final-Logik muss dynamisch sein. Wenn ein dritter Spieler (der nicht im Finale ist) eine Aktion ausführt (Einwürfeln), wird er permanent in die Liste der `activeFinalists` aufgenommen, bis er seine Chips verloren hat.

---

## 4. Technische Roadmap (Phasenmodell)

### Phase 1: Logic & Domain Refactoring (JETZT)
*   **DiceRoll.java:** Implementierung der neuen `compareTo`-Prioritäten.
*   **RoundEvaluator.java:** Logik für die "Goldene Mitte" (Wer wird übersprungen?).
*   **GameService.java:** Abstraktion der Wurf-Quelle (Virtual vs. Manual).

### Phase 2: Functional UI (MVP)
*   Erstellung eines einfachen HTML-Skeletts (Buttons & Text).
*   **Atomic Actions:** Ein Klick löst einen vollständigen Becher-Wurf (3 Würfel) aus.
*   Echtzeit-Chip-Monitor für den `CentralStack`.

### Phase 3: Real-Time & Polish
*   Umstellung auf **WebSockets (STOMP)** für synchrone Spielzüge.
*   Integration von HTMX für Teil-Updates der Seite (kein Full-Reload).
*   Visualisierung: Würfel-Bilder und Soundeffekte.

---

## 5. Geplante Features

- Regelkonformer Vergleich aller Würfe inkl. Hand-Status und Positionsregel (Goldene Mitte)
- Konfigurierbare Betriebsmodi: virtuell, haptisch (Schiri) und IoT/Remote
- Thrill-Mechanik: manuelles Aufdecken ("Lupfen") mit automatischer Strafchip-Vergabe bei Regelverstoß
- Dynamisches Finale mit Gefangen-Regel für unbeabsichtigt eingewürfelte Spieler
- Echtzeit-Multiplayer über WebSockets (STOMP)