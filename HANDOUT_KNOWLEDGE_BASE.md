# 🎓 Schocken Web App: Das Master-Handout für Java & Spring Architektur

Dieses Dokument dient als tiefgreifende Wissensbasis. Es erklärt nicht nur, *was* wir gemacht haben, sondern das *Warum* und *Wie* auf Senior-Level.

---

## 🍃 1. Spring Framework & Spring Boot

### @Transactional (Die "Alles-oder-Nichts"-Garantie)
*   **Was?** Eine Annotation, die eine Methode in eine Datenbank-Transaktion einpackt.
*   **Warum?** Stell dir vor, du überträgst 13 Deckel vom Stapel zu einem Verlierer. 
    1.  Schritt: Ziehe 13 vom Stapel ab.
    2.  Schritt: Füge 13 beim Spieler hinzu.
    *   **Das Problem:** Wenn nach Schritt 1 der Strom ausfällt, sind 13 Deckel im digitalen Nirgendwo verschwunden.
*   **Wieso?** `@Transactional` garantiert die **ACID-Eigenschaften** (Atomicity, Consistency, Isolation, Durability). Wenn irgendwo in der Methode ein Fehler (Exception) auftritt, macht Spring einen **Rollback**: Alle Datenbankänderungen seit Beginn der Methode werden rückgängig gemacht. Es ist, als wäre nie etwas passiert.

### Dependency Injection (DI) & Beans
*   **Was?** Spring verwaltet deine Klassen (als "Beans"). Du sagst nicht mehr `new PlayerService()`, sondern Spring "injiziert" (liefert) dir die fertige Instanz.
*   **Warum?** **Lose Kopplung**. Wenn der `PlayerController` den `PlayerService` braucht, fragt er Spring danach. Dadurch können wir für Tests einfach einen "Fake-Service" (Mock) unterschieben, ohne den Controller-Code zu ändern.

---

## 💾 2. JPA (Java Persistence API) & Hibernate

### Warum benutzen wir Annotationen wie `@Column` oder `@AttributeOverride`?
*   **Hintergrund:** Java-Objekte (Klassen) und Relationale Datenbanken (Tabellen) passen nicht perfekt zusammen (**Object-Relational Impedance Mismatch**).
*   **@Column:** Erlaubt uns, im Java-Code saubere Namen zu nutzen (`penaltyChips`), während die Datenbank hässliche alte Namen behält (`deckel`). Das ist wichtig für die Wartbarkeit.
*   **@AttributeOverride:** Wir nutzen das bei `DiceRoll`. Da `DiceRoll` in mehreren Entitäten vorkommen könnte, erlaubt uns diese Annotation, den Spaltennamen in der Tabelle der *einbettenden* Entität (z.B. `Player`) individuell zu benennen, obwohl das Feld in `DiceRoll` immer gleich heißt.

---

## ☕ 3. Modernes Java (Java 17+)

### Switch Expressions (In `DiceRoll.java`)
*   **Was?** Ein modernes `switch`, das einen Wert direkt zurückgeben kann.
*   **Der Code:**
    ```java
    return switch (type) {
        case "SHOCK_OUT" -> 5;
        default -> 1;
    };
    ```
*   **Warum?** 
    1.  **Kompaktheit:** Kein `break;` mehr nötig (die häufigste Fehlerquelle in altem Java-Code!).
    2.  **Vollständigkeit:** Der Compiler prüft (bei Enums), ob du alle Fälle abgedeckt hast.
    3.  **Ausdrucksstärke:** Es ist ein *Ausdruck* (Expression), keine *Anweisung* (Statement). Ein Ausdruck liefert ein Ergebnis.

---

## 🏗️ 4. Architektur-Konzepte

### DTOs (Data Transfer Objects)
*   **Was?** Eine Klasse, die nur Daten hält, ohne Logik.
*   **Wieso?** 
    1.  **Sicherheit:** Deine `Player`-Entity hat vielleicht Felder, die der Client (Frontend) nicht sehen darf (z.B. Passwörter, interne IDs).
    2.  **Stabilität:** Du kannst die Datenbank-Tabelle ändern (Entity), ohne dass sich die JSON-Struktur für das Frontend (DTO) ändert.

### SRP (Single Responsibility Principle)
*   **Was?** "Eine Klasse sollte nur einen Grund haben, sich zu ändern."
*   **Wieso?** Der `Player` sollte nur wissen, wer er ist. Wenn wir Spiellogik (wer hat die Hälfte verloren) in den `Player` packen, müsste sich die `Player`-Klasse ändern, wenn wir die Spielregeln ändern. Das ist falsch. Die Spielregeln gehören in einen `GameService` oder eine `GameSession`.

---

## 📚 Glossar für die nächste Prüfung

| Fachbegriff | Kurzerklärung |
| :--- | :--- |
| **Boilerplate** | Code, der immer wieder geschrieben werden muss, aber keinen echten Wert hat (z.B. Getter/Setter -> Lösung: Lombok). |
| **Impedance Mismatch** | Der Unterschied zwischen der Denkweise in Objekten (Java) und Zeilen/Spalten (SQL). |
| **Atomarität** | Eine Operation ist entweder ganz fertig oder gar nicht (Teil einer Transaktion). |
| **Inversion of Control (IoC)** | Nicht dein Code steuert das Programm, sondern das Framework (Spring) ruft deinen Code auf. |
| **Exhaustiveness** | Die Vollständigkeit eines Checks (z.B. alle Fälle in einem Switch). |
