# 📓 Developer Journal: Schocken Web App

This journal tracks our architectural decisions, daily progress, and key learning moments.

---

## [2026-03-20] Establishment of the Omni-Protocol & Project Audit

### 1. Was haben wir heute gebaut?
* **Omni-Protocol:** Einführung des neuen Workflows für die Zusammenarbeit zwischen User, Gemini (Architect) und Claude (Developer).
* **Professional Guidelines:** Die Projekt-Leitplanken (`GEMINI.md`, `CLAUDE.md`, `PROJECT_STATUS.md`) wurden auf ein professionelles, englisches Niveau gehoben.
* **Safety Protocol:** Einführung des "Read-Before-Write"-Prinzips, um unkontrollierte CLI-Änderungen zu verhindern.

### 2. Die wichtigsten Termini (für das Handout)
| Begriff | Warum ist er heute wichtig? |
| :--- | :--- |
| **SRP (Single Responsibility)** | Grundsatz, dass jede Klasse nur eine klar definierte Aufgabe hat (z.B. Trennung von Player und RoundEvaluator). |
| **Audit** | Ein systematischer Check des IST-Zustands (Files, Git-Status, Logs) vor Beginn neuer Entwicklungsphasen. |
| **Atomic Commits** | Das Ziel, Änderungen in kleinstmöglichen, logisch zusammenhängenden und stabilen Einheiten einzuchecken. |

### 3. Die "Aha!"-Momente & Design-Entscheidungen
* **Entscheidung:** Rollenteilung festgelegt. Claude agiert als "Lead Dev" (Ausführung), Gemini als "Senior Architect" (Strategie & Mentor).
* **Erkenntnis:** Die Reihenfolge der Commits (**Data -> Logic -> API**) ist entscheidend für die Stabilität. Man baut ein Haus vom Fundament auf.

### 4. Der nächste Meilenstein
* Analyse des Audit-Berichts von Claude.
* Erstellung des ersten technischen Entwurfs für den `RoundEvaluator`.

---