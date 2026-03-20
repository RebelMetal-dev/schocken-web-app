# Schocken-Web-App: Developer Guide

1. Terminal Navigation (Git Bash)
- STRG + L: Bildschirm leeren (Ordnung schaffen).
- STRG + C: Laufenden Befehl abbrechen (Not-Aus).
- STRG + U: Aktuelle Zeile komplett löschen.
- taskkill /F /IM claude.exe: Claude-Prozess via CMD beenden (bei Hängern).

2. Die Schocken-Commit-Formel (3-Fragen-Regel)
Jeder Commit-Body muss beantworten:
- Why: Warum war der alte Code problematisch?
- Trigger: Welches Szenario hat den Fehler/Bedarf ausgelöst?
- Behaviour: Wie verhält sich das System jetzt (besonders bei Fehlern)?

3. Schocken-Logik und Ranking
- STRAIGHT (Straße): 1-2-3, 2-3-4, 3-4-5, 4-5-6 (Fest verankerte Regel).
- Ranking-Folge: Schock > General > Straße > Hausnummer.

4. Git-Profi-Workflow
- Commit via File: git commit -F msg.txt (Sicherer Weg bei langen Nachrichten).
- Push: git push -u origin main (Sicherung auf GitHub).
- History: git log --oneline -n 5.

---
Dieser Guide wird kontinuierlich erweitert.
