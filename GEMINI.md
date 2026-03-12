# GEMINI.md

Dieses Dokument dient als zentrale Anlaufstelle für spezifische Anweisungen und Kontext für die Arbeit an der Schocken Web App.

## Projektübersicht
- **Name:** Schocken Web App
- **Technologie:** Java Spring Boot, Gradle
- **Zweck:** Digitalisierung des Würfelspiels "Schocken"

## Konventionen

## Mentoren
- **Java Mentor:** [.gemini/mentor/java_mentor.md](.gemini/mentor/java_mentor.md)
  - Dieser Mentor wird bei allen Java-spezifischen Fragen und Diskussionen hinzugezogen. Seine Regeln (z. B. Fokus auf Konzepte statt nur Code, Clean Code Best Practices) sind strikt zu befolgen.

### Git Commit Conventions

**MANDATORY** for all commits. All commit messages MUST be written in **English**. The subject line indicates *what* was changed. The body MUST answer these three questions:

1. **Why the old code was problematic** – what made it buggy, unstable, or incomplete.
2. **The triggering scenario** – the condition, input, or event that causes the bug/issue.
3. **The new behavior** – specifically for errors or edge cases.

**Format:**

`type: short subject (what was changed)`

`Why the old code was problematic: <explanation of the deficiency>.`
`Trigger scenario: <explanation of what causes the issue>.`
`New behaviour: <explanation of what happens now, especially in error cases>.`

**Example:**

`fix: prevent unbounded memory usage for oversized models.dev responses`

`Previously, toArray() buffered the entire HTTP response before parsing. A malformed payload or CDN anomaly could consume arbitrary memory with no explicit failure mode.`
`Now the response is streamed and aborted if it exceeds 5MB, making the failure explicit – it's routed to the existing error handler which logs a warning and briefly caches the failure to avoid hammering the API.`
