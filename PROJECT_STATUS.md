# Project Status: Schocken Web App (Refactoring & Game Logic)

## 🎯 Current Project Phase
**Phase 2: Transitioning from Player-Centric to Session-Centric Architecture.**
*Current focus: Decoupling transient game state from persistent player identity.*

---

## 🏗️ Architecture & Learning Handouts

### 1. Domain Modeling: Value Objects vs. Entities
*   **What?** We moved `DiceRoll` into an `@Embeddable` Value Object.
*   **Why?** A dice roll doesn't need its own ID in the database. It belongs strictly to a player's action. By making it a Value Object, we encapsulate logic (sorting dice, determining types like "SHOCK_OUT") inside the object itself.
*   **How?** Using JPA `@Embeddable` in the `DiceRoll` class and `@Embedded` in the `Player` class.
*   **Glossary:** 
    *   *Value Object:* An object that represents a descriptive aspect of the domain but has no identity (e.g., Color, Money, DiceRoll).

### 2. RESTful API & DTOs (Data Transfer Objects)
*   **What?** We use `PlayerController` with specific DTOs like `PenaltyUpdate`.
*   **Why?** Never expose your database entities directly to the API. If you change your database, you don't want to break the mobile app or website. DTOs act as a "contract" between the backend and the frontend.
*   **How?** Controller methods return `ResponseEntity<DTO>` instead of `ResponseEntity<Entity>`.
*   *Glossary:*
    *   *DTO:* A simple object used to pass data between software processes. It contains no business logic.

### 3. Database Compatibility & Clean Code
*   **What?** Renaming "Denglisch" fields (`deckel`, `istSicher`) to English (`penaltyChips`, `safe`) while keeping DB columns unchanged.
*   **Why?** Code must be professional and international. Databases are often "legacy" and cannot be easily changed without breaking other systems.
*   **How?** Using `@Column(name = "old_name")` to map clean Java names to ugly DB names.

---

## 🛠️ Pending Architectural Decisions (The "Devil's Advocate" Workshop)

### 🔴 The "Frankenstein-Player" Problem (Current Discussion)
*   **Problem:** Our `Player` entity currently holds game-specific data (`penaltyChips`, `lastRoll`, `lost_first_half`).
*   **Architecture Goal:** **Separation of Concerns (SoC)**.
*   **Proposed Solution:** Introduce a `GameSession` and a `GameParticipant` (Join-Entity).
*   **Terminology:**
    *   *SRP (Single Responsibility Principle):* A class should have one, and only one, reason to change.
    *   *SoC (Separation of Concerns):* Dividing a computer program into distinct sections such that each section addresses a separate concern.

---

## ✅ Completed Tasks (The Goldstandard)
- [x] Internationalization (English Naming)
- [x] @Embeddable DiceRoll logic
- [x] PenaltyUpdate DTO implementation
- [x] JPA Mapping with @AttributeOverride
