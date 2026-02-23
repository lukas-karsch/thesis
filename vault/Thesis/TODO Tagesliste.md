## Dienstag, 17.02 
- API Endpunkte anpassen, wie in TODO beschrieben 
## Montag, 23.02. 
- Scalability Metrik für alle Metriken beschreiben
- Data Store -> PostgreSQL und Axon Linien separat zeigen? 
- $CI\pm$ in data store statistical tests: alle neu berechnen
## Dienstag, 24.02 
- Load test: get lecture details 
- compare for different "enrollment" levels: how much is latency when 10, 20, 50 students are enrolled? 
- Load test: get lectures? Große Liste fetchen. 
## Mittwoch, 25.02 - Cleanup
- **Tabellen**
	- "averaged out over..." in appendix: Wording überdenken. Vlt einfach "aggregated"
	- "Speedup" in Tabellen -> "Ratio"
	- Schauen, dass $dropped\_iterations\_rate$ überall drin ist 
	- `timeseries_aggregated_statistical_tests` nochmal für alle laufen lassen. 
- **Visualizations**
	- recreate ALL visualizations because i changed "sd" to CI
	- mention that values are interpolated between the markers! 
	- Captions: Beschreibungen zu Namen von Metrik ändern
		- Threadpool -> $tomcat\_threads$ 
		- DB connections -> $hikari\_connections$ 
	- `log_x` immer gleicher Wert (`False`)! 
	- latency SLO in alle Grafiken einfügen! 
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
## Freitag, 27.02
- Kapitel 1: 
	- Motivation 
	- Structure of the thesis 
## Samstag, 28.02 
- Abstract 
## Sonntag, 01.03 
- Tabellen im Anhang aufhübschen 
- ist read_visible_rate früh genug erklärt? 
- insgesamt Logik überprüfen 