## Latex 
- Zitationsstil? 
	- aktuell: (Meyer 2006, p. 148)
- Deckblatt? 
- Wo ist die ehrenwörtliche Erklärung? 
- Formatierung? Aktuell LaTeX standard ohne weitere Konfiguration 
- Vorgabe für Länge des Theorieteils? 
- Wie sollte der "Related Work" Teil aussehen? Ähnliche Arbeiten nennen und zusammenfassen? 
- wie genau soll ich die Projekt requirements präsentieren? Also Business Regeln. UML Diagramm für Entities? Tabelle der Endpoint? Alle Entities erläutern? 
- Zitieren von source code dokumentation? JUnit, Axon? WIe Quellen? 
	- "Autor" von SpringBoot docs = SpringBoot, oder soll man da keinen Autor setzen? 
- Glossary passt? (zeigen)
	- vor oder nach dem content? 
- Unter Code snippets per footnote verlinken? 
## Fachlich 
- Flexibility anstatt Scalability:
  eigene "Metrik" erstellen, die z.B bewertet, wie leicht neue Features dazukommen: time to implement, new code, changes to existing code, new dependencies / coupling 
  -> immer noch sehr subjektiv
  -> wäre OK? 
- Ansonsten: flexibility in Richtung "functional evolution" -> beide Systeme bis zu Requirement A bauen; ein neues Requirement B einführen (welches neue historische Daten benötigt). Dann schauen, was nötig ist und ob das System irgendwas backfillen kann? 
- Oder Scalability: **Resource consumption (efficiency)**
   "Scalability" als Funktion der Effizienz - more CPU or RAM overhead, Disk I/O will require twice the hardware to scale to the same level; **Database Growth, Index performance**: Test read performance at 10k and 1M records, e.g. for SumAllCredits - does it slow down linearly in one application, but not in the other? **Write Contention / Lock analysis**:  Pessimistic locking in CRUD overhead? Compare maximum throughput on write operations -> increase VUs
- -> nicht "wirklich" skalieren (mit mehreren Nodes), aber analysieren, welches System die bessere performance ceiling hat anhand von contention, cpu und memory footprint
## Organisation 
- Bis wann kann ich einen Draft schicken? 
- 2-wöchige Besprechung mit fixem Termin? 
