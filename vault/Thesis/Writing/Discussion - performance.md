mention increased latency as soon as database and event store live on the network (in interpretation!
- especially for ES-CQRS app; because data travels from server -> event store -> server for projection processing -> postgres 
lookup projectors can be used outside of aggregates (message interceptor) to avoid blocking them while lookups are in progress -> only call aggregate once external validation is finished 
## What there would be to do to diagnose / identify more 
- Profiling 
	- e.g. JSON serialization overhead for es-cqrs app
- Test more than just load: how does latency change when _more_ data is being fetched at once? 
	- e.g. read-all-lectures: 20 lectures to be read vs. 50 lectures to be read. 
## CPU Usage 
Axon server and springboot share same CPU -> springboot usage shows "only" 60%, but axon server might take the rest -> in total, the system is overwhelmed. Should separate each service for better measurements. 
## Database connections 
- Why median of 0? 
### Get grade history
Why does the ES-CQRS application have such a high DB usage, when it should only look up the ID of the enrollment? There's indexes and no actual data is being fetched... 
## Improved projections 
- do not use JSON 
- use multiple tables which map directly (without JSON) to DTOs 
	- collections: e.g. "student_lectures_projection" with "studentId, lectureId, studentName, studentLastName"
	- fetch with `SELECT FROM student_lectures_projection WHERE lectureId in (lectureIds)`
	- quicker mapping (JDBC)
	- no JOINs, no JSON, denormalized database tables. Queries can be run in parallel 
- use binary formats like protobuf or Kryo. Store only `byte[]` in database. may be 5x-10x faster
