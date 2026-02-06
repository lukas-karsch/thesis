**Core architectural foundations:**
- Evans, Domain Driven Design 
- Betts et al: **_Exploring CQRS and Event Sourcing_. Microsoft patterns & practices.** This is a practical guide that discusses the transition from CRUD to CQRS
- Fowler, Event sourcing
- Young, CQRS documents 

**RS 1 - performance and scalability**
- Kleppman, 2017, data intensive systems 
	- Chapter 1: scalability, performance, approaches for coping with load 
	- Chapter 11 (stream processing). Performance implications of log structured storage vs. B-Trees (db index)
- Jogalekar et al, Evaluating the scalability of distributed systems
	- provides a formal mathematical framework for defining "Scalability" in your thesis, moving it from a buzzword to a measurable metric ($P = \lambda / T$).
- Singh, 2025 presents a performance comparison between DDD and CQRS. Yes, DDD is not "classical" CRUD architecture, but clearly the migration to separate read and write paths yielded a performance increase 
   ![[Pasted image 20260202152540.png]]
- Jayaraman et al, 2024 "Implementing Command Query Responsibility Segregation (CQRS) in Large-Scale Systems"
  includes a performance benchmark
  p. 58ff 

**RS 2 - architectural complexity, maintainability, flexibility**
- can talk about schema evolution
	- which is a non-goal of the thesis 
- Singh, 2025 **Using CQRS and Event Sourcing in the Architecture of Complex Software Solutions** 
	- specific results on code metrics: the transition to CQRS/ES increased the total number of classes (from 47 to 213) but decreased the overall cyclomatic complexity (from 534 to 522)
	- highlights that individual modules become simpler despite higher number of classes
- **Object Oriented Coupling based Test Case Prioritization** 
	- statistical approach (linear regression, hypothesis testing) to correlate OO metrics with software quality
	- Defines and analyzes the **CK Metrics Suite** (WMC, DIT, NOC, CBO, RFC, LCOM). It establishes empirical correlations, such as **Coupling Between Objects (CBO)** having the strongest negative impact on quality
- Deshpande et al. - 2020 - Object Oriented Design Metrics for...
  It can be seen that WMC, DIT, LCOM, LCOM3, MOA, MFA, CAM,  CBM, AMC, shown as *** as highest significance rate.* least significanceis NPM, MAX_CC and AVG-CC and lastly seen as. (dot) dam very low significant  whereas CBO, RFC, CA, CE, LOC, IC has *no significance in model building.* 
- **Comparative Study of the Software Metrics for the complexity and Maintainability of Software Development**
	- can act as "dictionary" for all the code metrics 
- **Fundamentals of software architecture** 
  Decision about which architecture should be chosen 
  Say what he decides (if he does, need to read)
- Abreu, Carapuça: MOOD metrics. 
__MOOD__
Proposed metrics by the authors: 
The Design of Eiffel Programs: Quantitative Evaluation Using the MOOD Metrics
Results based on EIFFEL standard libary. Small sample size, must be repeated 

| Metric                             | Proposed Range (90% CI) | Heuristic Shape         |
| ---------------------------------- | ----------------------- | ----------------------- |
| Method Hiding Factor (MHF)         | 15.4% - 38.7%           | Band-pass (Interval)    |
| Attribute Hiding Factor (AHF)      | 19.2% -35.5%            | High-Pass (Lower Limit) |
| Method Inheritance Factor (MIF)    | 60.6% - 77.1%           | Band-pass (Interval)    |
| Attribute Inheritance Factor (AIF) | 63.3% -  81.5%          | Band-pass (Interval)    |
| Coupling Factor (COF)              | 1.3% - 5.5%             | Band-pass (Interval)    |
| Polymorphism Factor (POF)          | 5.3% - 10.8%            | Band-pass (Interval)    |

**RS 3 - traceability** 
- Gantz, 2014: Basics of IT Audit 
Maybe include sources talking about GDPR and the _problems_ with a non-erasable event log? "right to be forgotten"
