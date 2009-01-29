Hallo,

Entwicklung/Installation

Die Anwendung ist als Eclipse-Projekt unter
https://www.brockmann-consult.de/svn/cs/download/trunk zu finden.

Sie ist (über META-INF/context.xml) so vorkonfiguriert, dass sie direkt
einsetzbar ist. (Au weia - Datenbankpasswort in SVN)

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