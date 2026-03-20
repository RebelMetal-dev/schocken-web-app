# Schocken-Web-App: Developer Guide

1. Terminal Navigation (Git Bash)
- STRG + L: Bildschirm leeren
- STRG + C: Laufenden Befehl abbrechen
- taskkill /F /IM claude.exe: Claude-Prozess via CMD beenden

2. Standard Git-Workflow
- git status: Status prüfen
- git add .: Alle Änderungen vormerken
- git commit -F msg.txt: Commit mit Nachricht aus Datei
- git push -u origin main: Auf GitHub sichern

3. History Management (Anonymisierung & Korrektur)
- git reset --hard HEAD~1: Den letzten Commit spurlos löschen (Vorsicht!)
- git push --force-with-lease origin main: Geänderte Historie sicher hochladen
- git commit --amend: Den letzten lokalen Commit nachträglich bearbeiten


