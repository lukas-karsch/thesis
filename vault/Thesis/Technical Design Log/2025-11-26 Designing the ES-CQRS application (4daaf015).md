While there are some open TODOs in the `impl-crud` application, it's about time I start working on the ES/CQRS app. It will be built using technologies I have no experience in, so there is a real risk - time to start!! 
## Architecture 
### Questions before designing 
- How to structure the application? (package structure, naming conventions)
	- must be valid DDD: [[Domain-Driven Design Quickly, A Summary of Eric Evans' Domain-Driven Design]]
- How to seed data? 
- How to check validity of inputs and return error codes? (especially when queries are necessary)
	- e.g. sendAndWait 
	- CommandInterceptors 
- How to save projections? 
	- can they have direct access to aggregates? 
	- save denormalized views or proper normalized tables with relationships?
	- denormalized:
		- very fast, no joins, no complex queries 
		- update problem: when a related entity changes, the denormalized JSON becomes invalid and needs to be updated (potentially complex)
			- maybe save references in the tables to quickly check whether the updated entity affects this entry of the projection 
	- normalized:
		- complex SQL and queries necessary, similar to the CRUD version
		- no update problem 
- How can aggregates have relationships to other aggregates? 
	- access from inside the aggregate? 
	- access from outside and injecting? 
	- creating via events -> link to other aggregates via ID 
- How to handle IDs? Need to create them on the application layer 
	- migrate DTOs to UUIDs 
	- _could_ generate unique IDs on the DB and ALSO create an aggregate ID (UUID)
- Eventual consistency
	- events might arrive at the event handlers (e.g. projections) out of order 
	- throw exceptions and use retry mechanisms to keep trying to create the projection 

**Design considerations:** https://docs.axoniq.io/bikerental-demo/main/implement-create-bike/
## Some answers
### Aggregates must NOT contain other aggregates 
- aggregates do NOT contain object graphs 
- they only contain references (via ID) to other aggregates, they must never hold direct object references
- they must enforce (only) their own invariants 
Validation:
> An aggregate may enforce invariants only over data it owns.  If validation requires other aggregates → do it in a process manager or the application layer.
- validation happens
	- in the application layer 
	- process manager / sagas for cross-aggregate workflows 
### Projections 
- save denormalized projections
- one view = one table
	- if anything, include value objects (@ElementCollection)
- when including JSON, I want to add a column like "referencedIds" that can be indexed
	- then, listen to e.g. RenameEvents and easily get the affected projections so they can be updated 
	- this is called **Reverse Lookup** or **Inverted Index** 
### IDs
- it might be necessary to migrate everything to UUIDs, so they can be created in the application layer (which is a MUST for CQRS)
- mention this in the #ddd downsides! 
### Validation Options 
- 1 ❌access the read model 
	- problems: eventual consistency, stale reads, timing errors 
- 2 ⚠️ Using a dedicated **consistency checker** projection
	- also a projection in the read model 
	- but very small, dedicated to consistency checks, e.g. if an aggregate exists 
	- can be indexed well
- 3 ✅ Pre-validate _using domain events_ rather than aggregate state
	- e.g. when only existence of an aggregate needs to be checked 
	- check that the event stream of an entity exists by peeking into it 
	- `eventStore.readEvents(courseId).peek()`
	- clean and fast 
	- but can not check internal state of aggregates 
- 4: (DONT USE) For critical rules: treat cross-aggregate invariants as _process_, not validation 
	- use a saga that listens for "CreateCourseRequested"
	- saga is allowed to load several aggregates 
	- saga can do validation:
		- all good -> send `CreateCourseCommand`
		- error -> send `CreateCourseRejected`
	- asynchronous 
	- But Saga is semantically not used to validate. It's to coordinate. -> Solution might be a subscription query 
- 5: use a dedicated SQL table like "student_reservations" with a unique constraint on student and lecture (projection)
	- performant (optimistic locking, can just `INSERT` and see if it succeeds)
	- downsides:
		- coupling of writes and reads 
		- several sources of truth again! 
	- can be called from command handler which can immediately return a status code
- 6: correction SAGA. Asynchronously react to events and if they're invalid, revert them
	- e.g. check for overlapping timeslots -> raise ForceUnenrollEvent(studentId: 1, lectureId: B, reason="Time conflict")
	- demonstrates eventual consistency 
	- feature of ES, purist 
	- raises more events 
	- good comparison point against CRUD
		- yes, the system was (shortly) in an invalid state 
		- but failed enrollments get more auditable (the log exposes the problem: ForceUnenroll because of time conflicts)
		- could lead to bad UI (can it be combined with subscription query?)
https://gemini.google.com/gem/2aaacb9c5388/39c8ffa826048b97
### Handle http responses 
- not possible to do proper validation and return a status code 
- send a `202 Accepted` and a status ID that can be polled via endpoint 
- Problem: writes will be very fast (obviously) but that makes for an unfair comparison to the CRUD system
	- if I choose the polling: must specify that performance metrics measure throughput, not latency 
- better solution might be subscription queries: [[AXON Subscription Queries]]
## What happens to my E2E tests? 
[[Testing in SpringBoot]]
The tests which check invariants and rely on status codes will break, because most of them are impossible to implement using DDD and CQRS. 
### What can I do about it? 
- Send requests and check the state in a subsequent `GET` request 
	- e.g try to enroll when it's forbidden, then send a `GET` request to check that the student is actually not enrolled 
- do not check status codes in `POST` and `PATCH`
- maybe include `wait()` in the tests to handle the eventual consistency 
	- if I ever have to await something, I should use `Awaitility`
- create a wrapper:
	- `assertStatus(Supplier<Integer> s, int status)`
	- wrap the API calls 
	- the CQRS test implementation can poll the status endpoint while the CRUD test implementation can directly supply the status code 
	- Downside: the tests will _not_ actually be identical and the APIs will not either - will my interfaces break? 
		- `ICourseController.java` etc. 
		- maybe remove the status code annotations on the interface 
	- However, it would be the most "semantic" thing to do because the CRUD API (which typically shouldn't rely on polling, especially when the endpoints should be synchronous) can then function the same way 
## Further Links 
- [[AXON - Event Sourcing Framework]]y