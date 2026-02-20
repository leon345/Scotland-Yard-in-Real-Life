# Scotland Yard in Real Life – Mobile Spielanwendung
Leon Seeger & Jannes Schophuis


# Zusammenfassung
Diese Projektarbeit befasst sich mit der erfolgreichen Umsetzung eines Scotland Yard Spiels als eine Mobile Anwendung, in der sich Spieler in einem selbst Definierten Bereich in ihrer Umgebung bewegen und den Mr. X finden müssen. Im Rahmen der Ausarbeitung werden die verwendeten Technologien sowie mögliche Probleme der Software erläutert.


# App downloaden
https://appdistribution.firebase.dev/i/a8f7769f4f254b1a

# Instalation
Die Installation der App kann wie bei einer generischen App stattfinden. Es müssen keine zusätzlichen Abhängigkeiten heruntergeladen oder Verbindungen erstellt werden. Das API-Level des ausführenden Gerätes muss mindestens Level 29 sein, was einer Android Version von Android 10 entspricht.

## Fastlane (Optional)
Fastalne kann unter macos mit [Homebrew](https://brew.sh/), einem Paketmanger installiert werden. 

`bash
brew install fastlane`


Für andere Betriebsysteme  kann die offizielle Instalationanleitung unter https://docs.fastlane.tools/ verwendet werden. 

### Einrichtung
Für die Bereitstellung der muss im Verzeichnis .secure_files ein Signierungsschlüssel angelegt werden sowie die Datei release-keystore.properties konfiguirert werden. 

storeFile=.secure_files/release-keystore.jks
storePassword=
keyAlias=release
keyPassword=



    