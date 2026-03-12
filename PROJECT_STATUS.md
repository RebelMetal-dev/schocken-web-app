# Project Status & Development Roadmap: Schocken Web App (March 2026)

## 1. Project Vision
A Spring Boot-based web application to digitalize the German dice game "Schocken".
- **Primary Goal:** Portfolio project for Junior Java Developer (Backend focused).
- **Secondary Goal:** Deepening knowledge of Spring Boot, JPA, Clean Code, and Internationalization (English coding standards).

## 2. Current Architecture (Status Quo)
- **Tech Stack:** Java 17, Spring Boot 3.x, JPA/Hibernate, H2 (In-Memory), Lombok.
- **Layers:** Controller -> Service -> Repository -> Model.
- **Key Features:** 
    - `DiceRoll` Value Object with automatic type detection (Schock, General, etc.).
    - Player Management with basic CRUD.
    - Virtual and Manual Dice Rolls.

## 3. The "Senior Architect's" Critique (Current Technical Debt)
*As identified by the "Devil's Advocate" Mentor:*
- **Denglish Code:** Mixed German/English naming (`addDeckel`, `letzterWurf`) is unprofessional.
- **Error Handling:** Use of generic `RuntimeException` is a bad practice for REST APIs.
- **Transaction Integrity:** Lack of `@Transactional` annotations on business logic methods.
- **Testing:** Missing automated unit tests for core game logic and edge cases.
- **Logging:** Use of `System.out.println` instead of professional SLF4J logging.
- **Magic Numbers:** Hardcoded values (e.g., "6" for dice sides) in services.

## 4. Refactoring Strategy (Junior-to-Senior Path)
### Phase 1: Foundation Clean-up (Current Focus)
1. **Internationalization:** Complete rename of all German entities, variables, and methods to English (e.g., `deckel` -> `penaltyChips`).
2. **Persistence Mapping:** Learning how to decouple Java field names from database column names using `@Column`.
3. **Exception Hierarchy:** Implementing `CustomExceptions` and a `@ControllerAdvice` for clean API error responses.
4. **Professional Logging:** Replacing all `System.out` with `@Slf4j`.

### Phase 2: Game Engine Logic
1. **GameService:** Introducing a centralized game engine to manage rounds and turns.
2. **Rule Implementation:** Automatic penalty distribution for "Schock Aus" (1,1,1).
3. **Winner/Loser Determination:** Utilizing `DiceRoll.compareTo()` for automated round results.

## 5. Learning Journal (Interview Prep)
- **Decoupling:** Why we separate Java names from DB columns (Backward Compatibility).
- **Atomicity:** Why `@Transactional` is critical for multi-user consistency.
- **Value Objects:** Why `DiceRoll` should be `@Embeddable` and Immutable.

---
*Status: In Refactoring (Denglisch Removal)*
