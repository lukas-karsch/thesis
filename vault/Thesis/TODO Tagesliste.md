## Montag, 23.02. 
- Data Store -> PostgreSQL und Axon Linien separat zeigen? 
## Mittwoch, 25.02
- **Tabellen**
	- `timeseries_aggregated_statistical_tests` nochmal für alle laufen lassen 
## Freitag, 27.02
- TODOs erledigen 
- **Discussion und Interpretation** fertig schreiben 
    - "Suggestions" 
    - RQs beantworten 
  - related work: andere sagen, write-performance schnell? (da append-only log)?
      - kann nicht repliziert werden
    - Arbeit als "Ergebnis" / Vergleich unter "realen" Bedingungen. Nicht nur rohe Benchmarks: 
	    - "Reines" CQRS gibt es eigentlich nicht 
      - die Write-Seite muss sich idR auf zusäzliche Mechanimsmen verlassen
          - "Append-only" event log, der sehr schnell sein sollte -> ja, append-only. aber zusätzlicher Overhead, z.B.
            durch validation, vor allem cross-aggregate. Das einfache schreiben ist NICHT der bottleneck --- gleich wie
            bei CRUD auch
	- Insgesamt hat CQRS mehr Pitfalls. Datenfluss ist nicht so nachvollziehbar 
	- Testumgebung hat Schwächen, ja. Aber Ergebnisse sind reproduzierbar, inkl. Anomalies 
## Samstag, 28.02 
- Abstract 
- Scalability metric für alle Tests in Anhang machen 
- Short caption für alle Tabellen überprüfen / hinzufügen 
- Anforderungen den Technologien zuordnen? 
- Related work > hervorheben, welche Teile relevant sind für meine Thesis / später wieder aufgreifen 
- pom.xml -> alle Versionen pinnen 
- unbedingt nochmal schauen, ob das mit dem signifikanztest so passt. zB read-all-lectures, tomcat_threads...
- Erklären, wie ich entschieden habe bis zu welcher load ich teste?
- Discussion > **Strengths of the Study** hinzufügen? 
## Sonntag, 01.03 

- Tabellen im Anhang aufhübschen
    - `\addlinespace` in Result Tabellen zwischen RPS
- ist read_visible_rate früh genug erklärt? 
- insgesamt Logik überprüfen 
- Unterschrift 
- Alle captions zu $metric\_name$ ändern?
- LICENSE?
