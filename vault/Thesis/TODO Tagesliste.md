## Montag, 23.02. 
- Data Store -> PostgreSQL und Axon Linien separat zeigen? 
## Mittwoch, 25.02 - Cleanup
- **URGENT**
	- read-all-lectures Ergebnisse holen 
- **Tabellen**
	- "averaged out over..." in appendix: Wording überdenken. Vlt einfach "aggregated"
	- "Speedup" in Tabellen -> "Ratio"
	- `timeseries_aggregated_statistical_tests` nochmal für alle laufen lassen 
- **Visualizations**
	- recreate ALL visualizations because i changed "sd" to CI
	- mention that values are interpolated between the markers! 
	- Captions: Beschreibungen zu Namen von Metrik ändern
		- Threadpool -> $tomcat\_threads$ 
		- DB connections -> $hikari\_connections$ 
	- `log_x` immer gleicher Wert (`False`)! 
	- latency SLO in alle Grafiken einfügen! 
- Letztes Kapitel: Discussion und Interpretation
## Donnerstag, 26.02 - Cleanup 
- Alle CPU Usage Daten müssen einheitlich sein! 
- Schauen, dass "dropped iterations" überall erwähnt wird wo iterations gedroppt wurden. 
- Scalability metric in "basics" schieben
- Schauen, ob man andere Sachen aus der Methodik auch in Basics schieben sollte 
	- z.B Static analysis metrics 
- RAM usage entfernen, da nirgends aufgeführt 
- L6: Time to consistency 
	- dropped iterations hat fehlende Werte 
	- read-visible-rate: table is missing 
- Dropped iterations rate -> turn into result for "failed requests rate".
	- Dropped = failed 
	- Make sure to turn this into SLO 3 
## Freitag, 27.02
- Kapitel 1: 
	- Motivation 
	- Structure of the thesis 
- Describe each load test in implementation chapter (which seed data, what is checked.)
## Samstag, 28.02 
- Abstract 
- Scalability metric für alle Tests in Anhang machen 
## Sonntag, 01.03 
- Tabellen im Anhang aufhübschen 
- ist read_visible_rate früh genug erklärt? 
- insgesamt Logik überprüfen 
