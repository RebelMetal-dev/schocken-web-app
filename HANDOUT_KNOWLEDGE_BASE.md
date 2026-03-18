# Schocken Web App: Das Master-Handout für Java & Spring Architektur

Dieses Dokument ist dein persönlicher "Survival-Guide". Es erklärt das "Warum" und "Wie" auf Senior-Level, aber mit Erklärungen für "Dummies", damit du es wirklich verstehst.

---

## 1. Spring Framework & Spring Boot

### @Transactional (Die "Alles-oder-Nichts"-Garantie)
*   **Für Dummies:** Stell dir vor, du kaufst ein Eis. Du gibst das Geld (Schritt 1) und bekommst das Eis (Schritt 2). Wenn der Verkäufer nach dem Geld-Nehmen wegrennt, ohne dir das Eis zu geben, macht `@Transactional` die Zeit rückgängig: Du hast dein Geld wieder, als wäre nichts passiert.
*   **Senior-Wissen:** Garantiert die **ACID-Eigenschaften**. Tritt ein Fehler auf, erfolgt ein **Rollback**.

### Dependency Injection (DI) & Beans
*   **Für Dummies:** Du brauchst einen Hammer. Statt selbst in den Baumarkt zu fahren und einen zu kaufen (`new Hammer()`), liegt der Hammer morgens einfach schon auf deinem Tisch, weil Spring ("Der Butler") ihn dir hingelegt hat.
*   **Senior-Wissen:** Sorgt für **lose Kopplung**. Klassen fordern Abhängigkeiten an, statt sie selbst zu erstellen.

---

## 2. Architektur-Konzepte (Die "Saubere-Code-Regeln")

### Kopplung (Coupling) – "Das Klettverschluss-Problem"
*   **Für Dummies:** Wenn zwei Legosteine mit Sekundenkleber zusammengeklebt sind, kriegst du sie nicht mehr auseinander, ohne sie kaputt zu machen (Starke Kopplung). Wir wollen aber, dass sie nur locker zusammengesteckt sind (Lose Kopplung).
*   **Warum?** Wenn wir den `Player` ändern, soll nicht plötzlich das ganze Spiel kaputtgehen. Der `Player` sollte "alleine" überleben können.

### Separation of Concerns (SoC) – "Die Küchen-Regel"
*   **Für Dummies:** In einem Restaurant kocht der Koch (Service), der Kellner bringt das Essen (Controller) und die Speisekarte sagt nur, was es gibt (Entity). Wenn der Koch auch noch bedienen muss, brennt die Suppe an.
*   **Warum?** Ordnung hält den Code wartbar. Jeder macht nur seinen Job.

### State Leakage (Zustands-Leck) – "Die schmutzige Weste"
*   **Für Dummies:** Du ziehst eine frische Hose an, aber in der Tasche ist noch der geschmolzene Schokoriegel von gestern. Das ist State Leakage. 
*   **Im Code:** Wenn wir Informationen wie `lostFirstHalf` im `Player` speichern, "klebt" dieser Status beim nächsten Spiel noch am Spieler. Wir wollen aber für jedes Spiel eine "frische Hose" (eine neue `GameSession`).

---

## 3. JPA (Java Persistence API) & Hibernate

### @Embeddable & @Embedded – "Das Paket im Paket"
*   **Für Dummies:** Du hast eine Adresse (Strasse, PLZ, Stadt). Statt drei einzelne Zettel zu haben, packst du sie in einen Umschlag (`@Embeddable`). In deinem Personen-Ordner (Entity) klebst du diesen Umschlag einfach ein (`@Embedded`).
*   **Warum?** In Java haben wir ein schönes `DiceRoll`-Objekt, aber in der Datenbank bleibt alles in einer einzigen, schnellen Tabelle.

### Dirty Checking – "Der faule Buchhalter"
*   **Für Dummies:** Hibernate ist wie ein Buchhalter, der den ganzen Tag dein Konto beobachtet. Wenn du heimlich einen Euro ausgibst, merkt er das sofort und schreibt es abends ins Hauptbuch, ohne dass du ihn extra anrufen (`repository.save()`) musst.
*   **Voraussetzung:** Das passiert nur innerhalb einer `@Transactional` Methode.

---

## 4. Java-Tools & Profi-Tricks

### Records – "Die unveränderliche Postkarte"
*   **Für Dummies:** Ein normales Objekt ist wie ein Notizbuch (man kann drin radieren). Ein `record` ist wie eine Postkarte, die mit Edding geschrieben wurde. Einmal fertig, kann man nichts mehr ändern (**Immutable**).
*   **Warum?** Perfekt für DTOs. Man kann sich sicher sein: Die Daten, die abgeschickt wurden, kommen auch genau so an.

### Enums – "Die feste Speisekarte"
*   **Für Dummies:** Statt zu sagen "Ich möchte was zu trinken" (wo alles Mögliche kommen könnte), sagst du: "Ich wähle von der Karte: COLA, WASSER oder BIER."
*   **Warum?** Verhindert Tippfehler. Der Computer meckert sofort, wenn du "WAZZER" schreibst.

---

## Glossar für Junior Dummies (Update)

| Fachbegriff | Erklärung für Dummies |
| :--- | :--- |
| **Immutable** | "Unkaputtbar". Einmal erstellt, kann man es nicht mehr ändern. Wie ein Stein. |
| **Refactoring** | Den Code innerlich aufräumen und schöner machen, ohne dass sich die Funktion ändert. |
| **Endpoint** | Die "Adresse" im Internet, unter der deine App erreichbar ist (z.B. `/api/players`). |
| **Payload** | Die eigentlichen Daten (die "Ladung"), die in einem Paket (JSON) verschickt werden. |
| **Mapping** | Die Übersetzung (z.B. Java-Feld `name` -> DB-Spalte `SPIELER_NAME`). |
| **Mocking** | Einen "Stuntman" (Dummy) für ein Objekt bauen, um es im Test zu simulieren. |
| **Boilerplate** | Langweiliger Code, der immer gleich ist und nur Platz wegnimmt (wie "Hochachtungsvoll" in Briefen). |
