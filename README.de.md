# Minimal Personal OS

<p align="right">
  <a href="README.md">🇬🇧 English</a> &nbsp;|&nbsp; <b>🇩🇪 Deutsch</b>
</p>

Minimalistischer Android-Launcher mit Basis-Apps für einfache Bedienung und bewussten Medienkonsum.

Entwickelt im Rahmen eines Master-Studienprojekts von **Matthias Gerhard** und **Max Timur Sönmez**.

Das Projekt geht von der Beobachtung aus, dass bestehende Vereinfachungsansätze für ältere Menschen oder Personen mit geringer Technikaffinität entweder drastisch Funktionen streichen oder ihre Hilfsmittel so tief im System vergraben, dass genau jene Fähigkeiten zur Einrichtung nötig sind, die der Zielgruppe fehlen. Minimal Personal OS ersetzt deshalb die komplette Startoberfläche, behält die Kernfunktionen aber erhalten und lässt sich über zwei visuelle Profile an wachsende Nutzererfahrung anpassen.

## Funktionsumfang

- **Launcher** als vollwertiger Home-Screen mit frei belegbaren Shortcut-Kacheln
- **Setup-Wizard** für die barrierearme Ersteinrichtung
- **Zwei visuelle Profile**: 
  - *Minimalistic*: rein textbasiert und fokussiert
  - *Clear and Simple*: große Icons mit eindeutig beschrifteten Schaltflächen
- Anpassbare Schriftgröße, Farbgebung, Helligkeit und akustische Rückmeldung (Audio-Feedback)
- **Telefon** mit Ziffernfeld und integrierter Kontaktverwaltung
- **Notizen** mit lokaler Persistenz
- **Galerie** für Fotos und Videos
- **Geschützte Einstellungen** mit PIN-Sicherung, damit Angehörige die Konfiguration übernehmen können, ohne dass sie im Alltag versehentlich verstellt wird

## Oberfläche

Dieselbe Startseite in beiden Profilen: Links reduziert auf Beschriftungen und eine schlanke Symbolleiste, rechts mit App-Icons und beschrifteten Schaltflächen für die vier Kernfunktionen.

| Profil *Minimalistic* | Profil *Clear and Simple* |
| --- | --- |
| <img src="screenshots/screenshot_minimal.jpg" alt="Startseite im minimalistischen Profil" width="420" /> | <img src="screenshots/screenshot_simple.jpg" alt="Startseite im Profil Clear and Simple" width="420" /> |

Die Einstellungen sind zweistufig aufgebaut: Was man gefahrlos anpassen kann, liegt offen. Alles, was die Grundkonfiguration betrifft, liegt hinter der PIN.

| Offene Einstellungen | Geschützte Einstellungen |
| --- | --- |
| <img src="screenshots/screenshot_settings.jpg" alt="Einstellungen mit Profilwahl, Schriftgröße und Helligkeit" width="420" /> | <img src="screenshots/screenshot_advanced.jpg" alt="Geschützte Einstellungen mit App-Slots, Kontakten und PIN-Schutz" width="420" /> |

## Technischer Überblick

Kotlin mit Jetpack Compose, Single-Activity-Architektur nach modernem MVVM-Muster. Die Navigation erfolgt über eine `Screen`-Enum in `MainApp.kt`.

```text
app/src/main/java/com/tisosoft/minimalpersonalos/
├── MainActivity.kt          Einstiegspunkt
├── ui/
│   ├── MainApp.kt           Navigation, globales Theming, Font-Skalierung
│   ├── screens/             Launcher, Phone, Notes, Gallery, Settings,
│   │                        ProtectedSettings, SetupWizard
│   ├── components/          Wiederverwendbare Dialoge und Bausteine
│   └── theme/               Farben, Typografie, Theme
├── viewmodel/               Gallery, Notes, Phone, Settings
├── data/                    App-, Contact-, Media-, Settings-Repository
│   └── notes/               Room-Datenbank für Notizen
├── model/                   AppInfo, MediaItem, VisualProfile
└── utils/                   Helligkeit, Ton, Zeit
```

Einstellungen liegen in `SharedPreferences`, Notizen in einer Room-Datenbank. Repositories kapseln die Zugriffe auf Android-Systemdienste wie `PackageManager`, `ContactsProvider` und `MediaStore`.

### Build-Konfiguration

| Bereich | Konfiguration |
| --- | --- |
| Sprache | Kotlin 2.4.10 |
| UI | Jetpack Compose (BOM 2026.08.00) |
| minSdk / targetSdk | 29 / 37 |
| JVM-Target | 17 |
| Gradle / AGP | 9.7.0 / 9.2.1 |
| Persistenz | Room 2.8.4 mit KSP |
| Bildladen | Coil 3.5.0 |

## Bauen und Ausführen

Voraussetzung ist Android Studio mit JDK 17+.

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Die App registriert sich als Home-Launcher (`android.intent.category.HOME`). Nach der Installation kann sie in den Android-Systemeinstellungen als Standard-Startbildschirm ausgewählt werden.

Benötigte Berechtigungen umfassen Kontakte, Telefonie, Medienzugriff und das Schreiben von Systemeinstellungen (für die Helligkeitsregelung).

## Lizenz

Dieses Projekt ist unter der [MIT-Lizenz](LICENSE) lizenziert.

