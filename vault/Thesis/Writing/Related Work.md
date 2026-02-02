**Core architectural foundations:**
- Evans, Domain Driven Design 
- Betts et al: **_Exploring CQRS and Event Sourcing_. Microsoft patterns & practices.** This is a practical guide that discusses the transition from CRUD to CQRS
- Fowler, Event sourcing

**RS 1 - performance and scalability**
- Kleppman, 2017, data intensive systems 
	- Focus on chapter 11 (stream processing). Performance implications of log structured storage vs. B-Trees (db index)
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
- **Comparative Study of the Software Metrics for the complexity and Maintainability of Software Development**
	- can act as "dictionary" for all the code metrics 

"To answer your question effectively:
1. **Use Source** **and** to establish your baseline comparison: ES tends to increase **Code Volume/Class Count** but decrease **Cyclomatic Complexity** and **Coupling**.
2. **Use Source****,****, and** to define the specific metrics (CK, MOOD, Martin's) you will analyze and the statistical methods (Regression) appropriate for them.
3. **Use Source** **and** to explain the architectural implications of your stats—e.g., why lower coupling in ES leads to better long-term flexibility despite higher initial boilerplate code."

**RS 3 - traceability** 
- Gantz, 2014: Basics of IT Audit 
Maybe include sources talking about GDPR and the _problems_ with a non-erasable event log? "right to be forgotten"