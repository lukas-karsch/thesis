## Formal
- muss man auf der Arbeit irgendwo eine Unterschrift platzieren? 
	- ja, muss drauf 
	- -> Adobe Acrobat 
- Dependency matrix
	- nötig, oder reicht pom.xml? 
	- muss ich hier _jede_ dependency eintragen? 
	- NICHT nötig 
	- Tag im Repository 
## Fachlich 
- Alle Testergebnisse als Tabelle in appendix? 
- Wie präzise sollte ich die Features der Anwendung beschreiben? 
	- einerseits sollte man ja alles, auf was man aufbaut, beschrieben haben 
	- anderseits sind die exakten business rules der app nicht wichtig für die research questions 
	- außer, der fakt dass writes consistent sein sollen 
## Feedback 
- Titel überlegen, NICHT als letztes 
Jordine, Messner, Akktikalmaz Bescheid geben 
- Matrikelnummer aufs Deckblatt 

Schaubilder sind gute Idee 
### Related Work
- Was sind die Key findings für meine Arbeit 
- auf die ich aufbaue? 
- Ergebnisse, die ich wieder in der Interpretation einfließen lasse 
### Method 
"Methodology" anstatt "Proposed Method"
- AUSSCHLIESSLICH vorgehensweise 
- NICHTS Richtung tech stack 

1. Anforderungsanalyse, funktional, nicht-funktional 
2. Implementierung anhand der Anforderungne von 2 Prototypen 
3. Dann Evaluation von Vergleich 
Diese 3 Phasen vorstellen: warum und wie 

Project requirements gehört NICHT in Methodik, nur definieren, _Dass_ man das macht, und wie die aussehen, damit man die Tests am ende machen soll 

Anforderungsanalyse wäre ein eigenes Kapitel "Requirements Analysis"
-> erst DANN Implementation 
nicht-funktional: "System muss so und so schnell reagieren"
muss nicht unbedingt jedes funktionale Requirement beschreiben. CRUD und CQRS addressieren 
wie habe ich das gemacht? 

in Implementation dann Abbildungen davon 

Environment (test computer) in evaluation, da es zum testsystem gehört 

Technologies von Methodik gehört in implementation 

Significance: welcher Test, und wieso? 

in der Methodik direkt aufführen, was Limitierungen, Schwächen sind 
### in Kapitel 7: "Threads of validity" als Section 
- z.B. nur demoprojekt
- könnte in großen Projekten anders aussehen 
- nicht im Cluster getestet

Forschungsfragen final beantworten 
