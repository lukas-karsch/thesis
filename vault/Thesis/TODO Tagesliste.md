## Dienstag, 17.02 
- API Endpunkte anpassen, wie in TODO beschrieben 
## Montag, 23.02. 
- Scalability Metrik "ausrechnen" für alle (falls möglich), dann schreiben 
- Data Store -> PostgreSQL und Axon Linien separat zeigen
- $CI\pm$ in data store statistical tests: alle neu berechnen
-  Schauen, dass alle performance Visualisierungen und Beschreibungen vollständig sind 
	- Create course prerequisites neu erstellen 
	- Inklusive Tabellen im Anhang
- Schauen, dass Static analysis Ergebnisse alle vollständig sind.
	- Inklusive Tabellen im Anhang! 
## Dienstag, 24.02 
- Load test: get lecture details 
- compare for different "enrollment" levels: how much is latency when 10, 20, 50 students are enrolled? 
## Mittwoch, 25.02 - Cleanup
- **Tabellen**
	- "averaged out over..." in appendix: Wording überdenken. Vlt einfach "aggregated"
	- "Speedup" in Tabellen -> "Ratio"
	- Schauen, dass $dropped\_iterations\_rate$ überall drin ist 
	- `timeseries_aggregated_statistical_tests` nochmal für alle laufen lassen 
- **Visualizations**
	- Add markers ("o") to ALL performance plots! 
	- recreate ALL visualizations because i changed "sd" to CI
	- mention that values are interpolated between the markers! 
	- Captions: Beschreibungen zu Namen von Metrik ändern
		- Threadpool -> $tomcat\_threads$ 
		- DB connections -> $hikari\_connections$ 
	- `log_x` immer gleicher Wert! 
	- latency SLO in alle Grafiken einfügen 
## Donnerstag, 26.02 - Cleanup 
- Alle CPU Usage Daten müssen in Prozent sein, ODER dezimal, aber einheitlich! 
- Schauen, dass "dropped iterations" überall erwähnt wird wo iterations gedroppt wurden. 
## Freitag, 27.02
- Kapitel 1: 
	- Motivation 
	- Structure of the thesis 
## Samstag, 28.02 
- Abstract 
