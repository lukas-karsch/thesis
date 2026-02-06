## Research question
To what extent can CRUD and ES-CQRS systems accurately and efficiently reconstruct historical states to satisfy business intent and compliance requirements? 
## Method 
### Accuracy of reconstruction
**Define qualitative criteria for comparison**
- **Source of Truth Integrity:** How the system guarantees that the history matches the current state. Especially in distributed systems (dual-write)
- **Intent Preservation:** The ability to distinguish between different business reasons for the same data change (e.g., "Refund" vs. "Fee").
- **Schema Resiliency:** How the history survives changes to the database structure or business rules. (?)

**Business metrics:**
These metrics are currently NOT in the paper. #todo
- root cause analysis 
- time to debug 
- security audit complexity and required work 
### Efficiency
- Project already employs load testing
- load test time-travel queries on both applications, assuming equal scenarios, equal amount of data etc. then compare performance 
- use cases 
	- grade history (already exists)
	  is rather simple as only the state of _one_ entity is needed 
	- some other question which requires to construct a larger part of the application, to answer a question 
	  e.g. which lectures was a student enrolled in at point $T$
- measure CPU and RAM usage 
	- fetching data from audit log is maybe efficient, because using date filters it can be limited exactly which rows to fetch from audit table 
	- event stream needs ALL past events to build the projection 
	- describe whether snapshots can be used when re-projecting, or if they are only used by the framework to rehydrate aggregates 
		- looks like thats not possible 
		- would need to store specific projections with "valid-from", "valid-to"
