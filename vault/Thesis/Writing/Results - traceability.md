## Accuracy - results from literature synthesis 
given the architectural constraints, an event log can perfectly recreate historic state, including user intent - by design. CRUD audit logs, however, are secondary source of truth artifacts, and defects in logic or somewhere else may lead to discrepancies which may not be identified immediately. 

-> so basically, assuming a "perfect" audit log, both systems can accurately reconstruct state; user intent may be lost; any sort of error in an audit log leads to corrupt history.
- "Second order artifact": audit log = what the application _says_ happened, vs  event store: primary source of truth, audit trail IS the database 
- User intent must be captured using additional context, meaning "by hand" enriching of log entries. ES collect intent by design
**Dual-write failure** 
- when audit log is written to a different database (in a distributed system), dual-writes have to be used 
- hard to make atomic 
- one write might fail, leaving the system inconsistent 
	- Designing data intensive applications, p. 452f 
- mention that when using a singular database, with the audit trail on that same DB (e.g. while using Envers), this does not happen 
**Schema evolution**
- this is more a topic for flexibility
- BUT, it also has implications for accuracy of ES reconstruction 
- not only does current state need to be migrated, it needs to be made sure that past state can be read correctly
	- Audit log: JSON blobs, old / outdated audit tables 
	- ES: Events need to be upcast 

**Business metrics** 
- literature suggests reduced time to debug when using event sourcing (root cause analysis)
- literature suggests up to 95% reduction in time spent preparing for security audits
- hard to find actual proof
	- Monagari, 2026 said this 
	- cites other papers not supporting his claims 
