# 🛠 Mein Schocken-Projekt Git & Terminal Cheat-Sheet

Dieses Dokument dient als Gedächtnisstütze für die Verwaltung des Repositories und die Arbeit im Terminal.

---

## 📂 1. Dateiverwaltung im Terminal (Windows PowerShell)

Da wir auf Windows arbeiten, nutzen wir die PowerShell-Befehle:

| Aktion | Befehl | Erklärung |
| :--- | :--- | :--- |
| **Datei erstellen** | `New-Item -Path "name.md" -ItemType File` | Erstellt eine leere Datei. |
| **Ordner erstellen** | `mkdir "ordnername"` | Erstellt einen neuen Ordner. |
| **Datei verschieben** | `mv "quelle" "ziel/"` | Verschiebt eine Datei in einen Ordner. |
| **Datei löschen** | `rm "dateiname"` | Löscht eine Datei (Vorsicht: permanent!). |
| **Inhalt auflisten** | `ls` oder `ls -R` | Zeigt Dateien (rekursiv auch in Unterordnern) an. |

---

## 🧹 2. Git "Hausputz" (Cleanup)

Wenn Dateien (wie `.idea/` oder `.iml`) fälschlicherweise auf GitHub gelandet sind, obwohl sie in der `.gitignore` stehen:

1. **Vom Git-Index entfernen (Datei bleibt auf PC):**
   `git rm -r --cached .idea`
   `git rm --cached schocken-web-app.iml`
   *(Erklärung: `--cached` löscht nur die Verknüpfung zu GitHub, nicht die echte Datei.)*

2. **Änderungen registrieren:**
   `git add .`

3. **Versiegeln & Hochladen:**
   `git commit -m "chore: cleanup repository and remove ide tracking"`
   `git push`

---

## 🚀 3. Täglicher Workflow (Standard)

1. `git status` -> Prüfen, was sich geändert hat.
2. `git add .` -> Alle Änderungen für den "Versand" vorbereiten.
3. `git commit -m "feat: beschreibung"` -> Den Stand lokal speichern.
4. `git push` -> Die Änderungen auf GitHub veröffentlichen.

---

## 🛡️ 4. Die `.gitignore` Strategie

**Was gehört NICHT ins Portfolio?**
*   **Sicherheit:** `.env` (API-Keys, Passwörter).
*   **Müll:** `build/`, `.gradle/`, `*.log` (Temporäre Dateien).
*   **Persönliches:** `.idea/`, `*.iml` (IDE-Einstellungen).

**Pro-Tipp:** Den Gradle-Wrapper (`!gradle/wrapper/gradle-wrapper.jar`) behalten wir drin, damit andere das Projekt ohne Installation starten können.

---

## 🧠 5. Architektonische Leitlinien

* **Canonical Form (Normalform):** Daten (wie Würfelwürfe) werden bereits im **Konstruktor** sortiert.
  * *Vorteil:* Das "Lager" ist immer ordentlich; der "Evaluator" muss nicht bei jedem Vergleich neu sortieren.
* **Struktur:** Das Hauptverzeichnis (Root) bleibt sauber. Alles Lesbare kommt in `/docs`, alles Interne in `/docs/internal`.