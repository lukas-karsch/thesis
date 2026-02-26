## Montag, 23.02. 
- Data Store -> PostgreSQL und Axon Linien separat zeigen? 
## Mittwoch, 25.02
- **Tabellen**
	- `timeseries_aggregated_statistical_tests` nochmal für alle laufen lassen 
## Donnerstag, 26.02
- **read-all-lectures**
	- Ergebnisse präsentieren
	- weitere Tests für 150 RPS, 200 RPS. Dann alle Metriken für CRUD abschneiden ab 250 RPS. 
- TODOs erledigen
- Letztes Kapitel fertig machen: **Discussion und Interpretation** 
	- CI bei read-only load tests sollte eigentlich 0 sein. Ist aber 0.01 bei ES-CQRS in `read-all-lectures` -> **muss in Interpretation erwähnt werden** 
## Freitag, 27.02
- Alle CPU Usage Daten müssen einheitlich sein! 
- dropped_iterations_rate
	- Überall erwähnen! (TODO: L5 read all lectures) 
	- Dropped iterations rate -> turn into result for "failed requests rate".
		- Dropped = failed 
		- Make sure to turn this into SLO 3 
- Enrollment: requests fail because of timeout. 
## Samstag, 28.02 
- Abstract 
- Scalability metric für alle Tests in Anhang machen 
- Short caption für alle Tabellen überprüfen / hinzufügen 
- Anforderungen den Technologien zuordnen? 
- Related work > hervorheben, welche Teile relevant sind für meine Thesis / später wieder aufgreifen 
- pom.xml -> alle Versionen pinnen 
- unbedingt nochmal schauen, ob das mit dem signifikanztest so passt. zB read-all-lectures, tomcat_threads...
## Sonntag, 01.03 
- Tabellen im Anhang aufhübschen 
	- resizebox entfernen, wann immer möglich 
- ist read_visible_rate früh genug erklärt? 
- insgesamt Logik überprüfen 
- `\addlinespace` in Result Tabellen zwischen RPS 
- Unterschrift 
- Alle captions zu $metric\_name$ ändern
