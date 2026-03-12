# GEMINI.md

Dieses Dokument dient als zentrale Anlaufstelle für spezifische Anweisungen und Kontext für die Arbeit an der Schocken Web App.

## Projektübersicht
- **Name:** Schocken Web App
- **Technologie:** Java Spring Boot, Gradle
- **Zweck:** Digitalisierung des Würfelspiels "Schocken"

## Konventionen

### Git-Commit-Konventionen

**ERFORDERLICH** für alle Commits. Die Betreffzeile gibt an, *was* geändert wurde. Der Textkörper MUSS alle drei Fragen beantworten:

1. **Warum der alte Code ein Problem war** – was ihn fehlerhaft, instabil oder unvollständig gemacht hat.
2. **Welches Szenario das Problem auslöst** – die Bedingung, Eingabe oder das Ereignis, das den Fehler/das Problem verursacht.
3. **Wie das neue Verhalten ist** – insbesondere bei Fehlern oder Grenzfällen.

**Format:**

`typ: Kurzer Betreff (was geändert wurde)`

`Warum der alte Code problematisch war: <Erklärung des Mangels>.`
`Auslösendes Szenario: <Erklärung, was das Problem verursacht>.`
`Neues Verhalten: <Erklärung, was jetzt passiert, insbesondere im Fehlerfall>.`

**Beispiel:**

`fix: Verhindert unbegrenzten Speicherverbrauch bei übergroßen models.dev Antworten`

`Zuvor hat toArray() die gesamte HTTP-Antwort vor dem Parsen gepuffert. Ein fehlerhafter Payload oder eine CDN-Anomalie konnte beliebig viel Speicher verbrauchen, ohne expliziten Fehlermodus.`
`Jetzt wird die Antwort gestreamt und abgebrochen, wenn sie 5 MB überschreitet, wodurch der Fehler explizit wird – er wird an den vorhandenen Error-Handler weitergeleitet, der eine Warnung protokolliert und den Fehler kurzzeitig zwischenspeichert, um die API nicht zu überlasten.`
