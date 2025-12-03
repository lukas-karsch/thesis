#cqrs

CQRS, or ==Command Query Responsibility Segregation==, is an architectural pattern that separates a system's responsibilities for handling **commands** (which are actions that change state) and **queries** (which retrieve data). By splitting these two distinct operations, each can be optimized independently for performance, security, and scalability. For example, commands can be handled by one data model and queries by another, potentially using different technologies optimized for each task. (https://learn.microsoft.com/de-de/azure/architecture/patterns/cqrs)
## Technologies 
CQRS works great with [[SpringBoot]] and [[AXON - Event Sourcing Framework]].
## Considerations 
- **Commands** can be handled asynchronously 
	- This does mean that when sending a command, usually there is no reply 
- **Queries** are handled via **projections** (because they _project_ events onto a specific view)
	- allows for very fast queries
	- can just create new views on existing data 
	- can drop views without losing any data 
- **Commands and Queries (=reads) should be separated**, but that "pure" approach is not always possible 
	- when enforcing business rules via several aggregates (e.g. in my case, check if a student has enrolled in another lecture that has an overlapping timeslot)
	- projections or specific index tables have to be injected into the service layer, when necessary, to check those rules 
	- however, the view side is _eventually consistent_ -> might lead to mistakes 
	- use optimistic concurrency control (write on a version of the aggregate, if the version has changed due to another action in the system, throw an exception)
- **Saga**
	- long-lived, stateful process manager that reacts to events and sends commands in response, coordinating workflow over time
	- domain process
	- coordinate multiple aggregates: When an order is placed → reserve inventory → charge payment → ship order  
	- long-lasting process 
	- asynchronous process (by nature) -> waiting for events 
	- compensating actions (e.g. system was put into an illegal state)
- **Sagas are not**: 
	- place for synchronous validation 
	- a fix for stale projections that were used in the command path 
	- a second attempt at validating business rules (should belong in retry logic in the command layer)
	- place to enforce rules that should have been guaranteed at write-time 
## Links
- Inter aggregate communication: https://danielwhittaker.me/2014/11/22/4-secrets-inter-aggregate-communication-event-sourced-system/
- Cross aggregate validations, several options with tradeoffs https://medium.com/@arsalan.valoojerdi/cross-aggregate-validations-exploring-set-based-validation-techniques-in-event-sourcing-e28d9e0ffce6