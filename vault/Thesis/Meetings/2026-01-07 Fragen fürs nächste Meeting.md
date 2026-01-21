## Latex 
- Zitationsstil? 
	- aktuell: (Meyer 2006, p. 148)
	- Jordine kennt sich mit IEEE und AP7 aus 
- Deckblatt? 
	- gibt von der Hochschule vorgegeben 
- Gibt es eine vorgefertigte ehrenwörtliche Erklärung? 
	- gibt von der Hochschule vorgegeben 
- Formatierung? 
	- -> Aktuell LaTeX standard ohne weitere Konfiguration 
	- 11pt, Zeilenabstand 1,5; frei verfügbare Schriftart 
- Vorgabe für Länge des Theorieteils? 
- Wie sollte der "Related Work" Teil aussehen? Ähnliche Arbeiten nennen und zusammenfassen? 
- wie genau soll ich die Projekt requirements präsentieren? Also Business Regeln. UML Diagramm für Entities? Tabelle der Endpoint? Alle Entities erläutern? 
- Zitieren von source code dokumentation? JUnit, Axon? Wie Quellen? 
	- "Autor" von SpringBoot docs = SpringBoot, oder soll man da keinen Autor setzen? Autor als Organisation 
	- schaue in AP7 nach 
	- Tools: In der Fußnote URL zur Hauptseite des Tools 
- Glossary passt? (zeigen)
	- vor oder nach dem content? 
	- -> soll vor den Content 
- Unter Code snippets per footnote verlinken? 
	- Listings als Caption, sonst als Fußnote 
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

CPU und RAM usage: ausreichend? 
- Literatur anschauen: gibt es dazu Annahmen? 
- Prozess CPU Auslastung messen, nicht System 

Flexibilität
- o reilly: Buch zur BEwertung von Architekturmetriken #todo 
- bulding evolutionary architectures
- https://www.thalia.de/shop/home/artikeldetails/A1064052964?ProvID=15326610&gad_source=1&gad_campaignid=23444783204&gbraid=0AAAAADwkCX76qejWp0TkqezH7a_teg-uh&gclid=CjwKCAiAybfLBhAjEiwAI0mBBkb8ACfYW27g6W88wwPaQvpvvkPjPZOi-F1W82Ushy4Fj3DlcleoeBoCYrcQAvD_BwE
