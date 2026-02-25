## Instability and Abstractness, Main Sequence 
ES-CQRS has several packages close to the main sequence. But those packages are not really abstract, they are the packages containing several repositories. Because JPA repositories are interface classes. This shows a weakness of the metric. 

Also, the instability and abstractness metrics are very sensitive to package design. Just depending on the "package layout" that is chosen, the metrics may look very different... 

Abstractness DOES NOT equal abstraction. 

the "Interface" of the ES-CQRS app, in the `api` package, leads to low coupling between command and read side. But the classes are highly depended on, and not abstract at all, making their metric values bad. 
