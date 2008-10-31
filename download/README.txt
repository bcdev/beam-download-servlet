Hallo,

ich habe die Downloadanwendung installiert. Hier erstmal die
Bedienungsanleitung, unten die Installations- und Entwicklungsdokumentation:


******
Wichtig für Neustart des Servers: Wenn bcserver6 neu gestartet wird,
muss neben den üblichen Tomcats jetzt auch wieder /opt/tomcat6 neu
gestartet werden.
******

Downloadbar sind alle Dateien in /fs1/beamdownloads/downloads - sofern
der Webserver (merci@bcserver6) Zugriff darauf hat. In
/fs1/beamdownloads/GeoLiteCity.dat liegt die Datenbank mit den Geodaten
für die örtliche Auflösung der IP-Adressen. Es gibt monatlich neue
Versionen unter http://www.maxmind.com/app/geolitecity

Downloads, die protokolliert werden sollen, müssen also in das o.g.
Verzeichnis kopiert werden. Die URL für den Download ist dann:
http://www.brockmann-consult.de/download/?what=dateiname

Wenn eine Datei angegeben wird, die nicht existiert, wird der Besucher
auf http://www.brockmann-consult.de/beam/download weitergeleitet.

Die dargestellte Seite (das Formular) sollte sich einigermaßen als Frame
in die BEAM-Seite einpassen.

Ergebnisse landen in der MySql Datenbank auf bcoracle1, Datenbank
"download", Benutzer "download", Passwort "ichbindiedownloadanwendung"

Der Text und das Formular lassen sich nur in der Anwendung ändern - ich
baue noch eine Version, die das Land bereits vor-befüllt, das halte ich
für besser als eine Auswahlliste... Macht dem Benutzer nur Arbeit und
gibt uns dadurch schlechtere Ergebnisse.



Entwicklung/Installation

Die Anwendung ist als Eclipse-Projekt unter
https://www.brockmann-consult.de/svn/cs/download/trunk zu finden.

Sie ist (über META-INF/context.xml) so vorkonfiguriert, dass sie direkt
einsetzbar ist.

Die Implementation ist äußerst plump: Es gibt ein großes
Downloadservlet, das alles macht. Das kann man zwar bedeutend eleganter
machen, schien mir für so einen einmal verwendeten Schnellschuß aber
nicht ratsam.

Es gibt keine externen Abhängigkeiten - die Geo-IP-Auflösung habe ich im
Quelltext mit in SVN übernommen - so wurde sie auch ausgeliefert.

Wenn mir jemand bei der Maven-Einrichtung hilft, baue ich auch gern noch
ein POM ein - zumindest habe ich die Dateistruktur schonmal an den
Maven-Standards orientiert, allerdings sind auch die für Eclipse
notwendigen Konfigurationsdateien in svn enthalten.

Aus dem Eclipse-Projekt ist mittels Export/As WAR file die vorgefundene
Webanwendung geworden. Am besten läuft sie unter Java6 (mit Java5 hatte
ich Probleme mit der Classfile-Version - eigentlich nicht
nachvollziehbar aber da einer der Tomcats auf Java6 läuft, habe ich halt
den genommen)

Ich hoffe, ich habe nichts vergessen. Diese Mail wird auch als
README.txt noch in SVN gespeichert.

Olaf