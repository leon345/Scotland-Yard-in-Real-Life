# Scotland Yard in Real Life – Mobile Spielanwendung
Leon Seeger & Jannes Schophuis

# App downloaden
https://appdistribution.firebase.dev/i/a8f7769f4f254b1a

# Kurzbeschreibung
Die Android-App soll das klassische Brettspiel Scotland Yard als Outdoor-Erlebnis in die Realität übertragen. Spieler können ein Polygon auf einer Karte einzeichnen, das als Spielfeld dient. Während des Spiels sehen Detektive den Live-Standort der anderen Mitspieler in Echtzeit. Zur Orientierung können vorab Schlüsselpunkte oder Wegpunkte über Maps ausgewählt und in der App zwischengespeichert werden. Für die Kartendarstellung wird ein geeignetes Karten-SDK evaluiert und integriert. Die Synchronisation der Spielerdaten sowie die Authentifizierung werden über Google Firebase abgewickelt, um eine zuverlässige Echtzeit-Kommunikation zwischen den Spielern zu gewährleisten. 


# Musskrieterien
- Karten View erstellen
- Ein Sielfeld auf der Karte definieren können. (Polygon)
- Spieler können ein Spiel erstellen
- Speiler können andere Spieler zu einem Spiel einladen
    -  Per Link oder Code
- Speilanpassungsmöglichkeiten:
    - Zeitintervall für den Banditen
    - Spieldauer
    - Anzahl der Fähigkeiten des Banditen (Wie offt jede eingesetzt werden darf)
- Beim erstellen eines Spiels, wird jedem Spieler eine ID inkremtel zugeteilt. 
    - Der Scope der ID ist nur in einem Spiel eindeutig. Nicht global
- Ein Spieler kann nur in einem Spiel sein
- Es werden keine Spielergebnisse auf einem Server persistiert. 
- Ein Spieler muss die Möglichekit haben seinen Standort zur Laufzeit eines Spiels zu übertragen
    - Live automatisch in kurzen Zeitintervall (5 sec).
- Ein Spieler kann alle Positionen, der anderen Detektive, live auf der KArte sehen. 
- Der Standort des Banditens wird alle x Secunden anzeigt auf der KArte. 
    - Der letzte beaknnte Standort des Banditen wird auf der Karte angezeigt. 
- Beim Auslösen einer Fähigkeit werden die Detektive Benachrichtigt
- Spieler haben die Möglichkeit den Banditen als gefunden zu markieren. Dadurch ist das Spiel beendet


# Wunschkriterien
- Detektive können Punkte setzen, wo der Detektive Vermutet wird. Der Bandit kann die Punkte nicht sehen. 
- Zu jedem Spiel gibt es einen Chat zum Kommunizieren
    - ein Chat mit Bandit 
    - ein Chat ohne Bandit
- Spielmodi (optional):
    - automatisch Wechsel von Detektiv zu Bandit. Der Detektiv der den Banditen gefasst hat, wird autmatisch zum neuen Banditen
    