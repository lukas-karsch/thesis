This file contains my final architectural decisions on how to build the ES-CQRS App. 
## Code structure / architecture
Combine vertical slices with CQRS (= separated reads and writes)
```
com.lukaskarsch.university
├── config                
├── core  
└── features
    ├── enrollment
    │   ├── api                     # accessible throughout the application
    │   │   ├── commands.kt
    │   │   ├── events.kt 
    │   │   ├── queries.kt 
    │   │   └── dtos.kt 
    │   │   
    │   ├── command
    │   │   ├── lookup
    │   │   │   ├── EnrollmentLookupProjector.java  # Command-side lookups 
    │   │   ├── CourseAggregate.java   # The ES Aggregate
    │   │   └── CourseService.java     # Optional: orchestration only!
    │   │   
    │   ├── query
    │   │   ├── StudentCreditsProjectionEntity.java  # Entity for Read DB
    │   │   ├── StudentCreditsRepository.java  # Spring Data Repo
    │   │   └── StudentCreditsProjector.java     # Event Handlers & Query Handlers
    │   │   
    │   └── web              
    │       └── EnrollmentController.java
    │
    └── grading              
        ├── api
        ├── command
        ├── query
        └── web
	...
```
## Services, Validation
Validation and enforcing "ACID" is one of the hardest things in CQRS, because the architecture is inherently eventually consistent. There are patterns to get "good enough" consistency (e.g. 1)

Overview by Gemini.
### 1. The "Parameter Injection" Pattern (Easiest)
You validate the prerequisite _before_ sending the command, or you pass the proof of the prerequisite _into_ the command. This puts the burden on the client or a service to fetch the necessary data first.

- **How it works:** The Controller (or a thin Service) queries the `StudentCreditsProjection` first. If the student has enough credits/prerequisites, it adds that info to the command.
- **The Code:**
```java
public void enroll(String studentId, String courseId) {
	// 1. Query the Read Model (Fast, eventually consistent)
	List<String> passedCourses = queryGateway.query(
		new GetStudentHistoryQuery(studentId), ...
	).join();

	// 2. Validate prerequisites here (or pass the list into the command)
	if (!passedCourses.contains("MATH_101")) {
		throw new ValidationException("Prerequisites not met");
	}

	// 3. Send Command
	commandGateway.send(new EnrollStudentCommand(courseId, studentId));
}
```
- **Pros:** Simple, keeps Aggregates clean.
- **Cons:** Not strictly consistent. Between step 1 and 3, the student's history _could_ theoretically change (unlikely in this domain, but possible in others). It relies on the Read Model being up to date.
### 2. The "Saga" (Process Manager) Pattern (Most Correct)
If the validation is complex or requires strict consistency (e.g., reserving money in one account before crediting another), use a Saga. A Saga manages a long-running transaction across multiple aggregates.
- **How it works:**
    1. `Course` aggregate receives `RequestEnrollmentCommand`. It reserves a seat tentatively and emits `EnrollmentRequestedEvent`.
    2. **Saga** listens to this event. It sends a command to the `Student` aggregate: `VerifyPrerequisitesCommand`.
    3. `Student` aggregate checks its own history. If valid, it emits `PrerequisitesVerifiedEvent`.
    4. **Saga** listens to _that_ event and sends `ConfirmEnrollmentCommand` back to the `Course`.
- **Pros:** Eventually consistent, robust, handles rollbacks (compensating actions) if the check fails.
- **Cons:** High complexity. For a Bachelor's thesis, this might be overkill unless "complex cross-aggregate transactions" is a specific goal.
### 3. The "Interceptor / Dispatch Interceptor" Pattern (The "Cleaner" Alternative to #1)
This is a sophisticated version of Option 1 using Axon's infrastructure. You can intercept the command before it reaches the Aggregate to perform validation against a projection.
- **How it works:** You write a `MessageDispatchInterceptor`. Before the `EnrollStudentCommand` is dispatched to the Aggregate, the interceptor checks the `StudentCreditsProjection` database.
- **The Code:**
```java
public class PrerequisiteInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {
    private final StudentRepository studentRepo; // Read model repo
    
    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> messages) {
		return (index, command) -> {
			if (command.getPayload() instanceof EnrollStudentCommand) {
				EnrollStudentCommand cmd = (EnrollmentCommand) command.getPayload();
				// Check DB
				if (!studentRepo.hasPassed(cmd.studentId(), "MATH_101")) {
					throw new IllegalStateException("Prerequisites missing");
				}
			}
			return command;
		};
	}
}
```
### 4. Set Based Validation
Create a lookup table that belongs to the command side and is immediately consistent. 
[[2025-12-06 I discovered set based validation]]
This lookup table can be injected into command handlers. 
### Decision
**Use Option 4 (Lookup table Check).**
### Projections 
- save denormalized projections
- one view = one table
	- if anything, include value objects (@ElementCollection)
- when including JSON, I want to add a column like "referencedIds" that can be indexed
	- then, listen to e.g. RenameEvents and easily get the affected projections so they can be updated 
	- this is called **Reverse Lookup** or **Inverted Index** 
	- add @JsonIgnore 
## Endpoints 
To keep the APIs identical, I want my rest adapter to be synchronous. Basically, the backend stays async, in Axon fashion, and the REST layer is like a synchronous frontend.
This can be achieved using [[AXON Subscription Queries]].

However, this is not typical for CQRS applications and might lead to additional latency. I will have to think about how this can be presented in the thesis and how it would influence my performance metrics.
## Aggregates
### 1. Course 
Fields:
- @AggregateIdentifier id 
- credits 
- prerequisiteCourses 
- minCredits 
### 2. Lecture 
Fields:
- @AggregateIdentifier id
- courseId 
- totalSeats 
- enrolledStudents: `List<UUID>`
- assessments: `List<AssessmentValueObject>` 
- status: OPEN_FOR_ENROLLMENT, ... 
#### 2.1 AssessmentValueObject
Fields: 
- date 
- weight 
### 3. Enrollment 
Fields: 
- @AggregateIdentifier id
- lectureId 
- studentId 
- grades: `Map<AssessmentId, Integer>`
- isCreditsAwarded
#### 3.1 Why "isCreditsAwarded"?
You asked: _"Are they necessary for further business logic, considering that the 'validation' happens by querying projections?"_

**Answer:** You are correct that _future_ validation (e.g., checking prerequisites for the _next_ course) will happen via Projections. The Aggregate does not need to store the credit value to help _other_ aggregates.

**However**, the Aggregate **must** store the `isAwarded` flag to help **itself**:
1. To prevent double-awarding (Idempotency).
2. To lock the decision in history as an immutable fact, protecting it from future rule changes (Historical Consistency).
## Links
- [[2025-11-26 Designing the ES-CQRS application (4daaf015)]]
