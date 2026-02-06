How can i measure or compare flexibility? 
https://notebooklm.google.com/notebook/1f2b1c35-7853-47c6-ab00-568a434fa262
>Flexibility in software architecture is often quantified through proxies such as **modularity**, **evolvability**, **maintainability**, and **coupling**.

**Afferent and Efferent Coupling Ca; Ce:** measure the direction and volume of dependencies between your components (e.g., between your API layer, domain logic, and persistence/audit layers).
    ◦ **Afferent Coupling** The number of incoming connections to a code artifact.
    ◦ **Efferent Coupling:** The number of outgoing connections to other artifacts.
• **Instability** This metric determines the volatility of a codebase. It is calculated as I=Ce/(Ce+Ca). A value near 0 indicates a very stable (hard to change without breaking others) component, while a value near 1 indicates a highly unstable (easy to change but breaks easily) component.
• **Abstractness**  
• **Distance from the Main Sequence D
- Cohesion metrics 
	- LCOM
- Modularity Maturity Index (MMI)
- Connascence 
- Architectural fitness functions 
- Cycle Time 
-> Calculated some metrics in IntelliJ using a plugin; maybe visualize them to see if there is any difference 
## Opinion
Looks like there are some metrics in literature which propose metrics to "measure" the flexibility of a software architecture. But they seem to rely a lot on static code analysis; and e.g. measuring the "abstractness" is not really a great advisor for how flexible a package is. (See http://odrotbohm.de/2024/09/the-instability-abstractness-relationsship-an-alternative-view/)
## Links 
- [[2026-01-29 Visualizing flexibility metrics]]
