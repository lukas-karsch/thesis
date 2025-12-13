When unit testing `LectureAggregate`, I ran into a problem: the aggregate created a childAggregate (`EnrollmentAggregate`) via `createNew`. This worked, but the command handler created its own ID for the new aggregate. 

This child aggregate in turn emitted an event (`EnrollmentCreatedEvent`) with an unpredictable ID.
## A saga helped 
To fix the problem, I created `EnrollmentApprovalSaga`. It listens for when an aggregate _approves_ the enrollment (via `@SagaEventHandler`) and emits a command to create the enrollment aggregate. 

It then listens to this child aggregate's `EnrollmentCreatedEvent` to end the saga and confirm the creation to the original `LectureAggregate` which will then emit a `StudentEnrolledEvent` to be used in its `@EventSourcingHandler`.
## Code 
```java
@Saga   
public class EnrollmentApprovalSaga {  

	@Autowired
	private transient CommandGateway commandGateway;

    @StartSaga  
    @SagaEventHandler(associationProperty = "lectureId")  
    public void handle(StudentEnrollmentApprovedEvent event) {  
        UUID enrollmentId = UuidUtils.randomV7();  
  
        commandGateway.send(  
                new CreateEnrollmentCommand(enrollmentId, /* ... */)  
        );  
    }  
  
    @EndSaga  
    @SagaEventHandler(associationProperty = "lectureId")  
    public void handle(EnrollmentCreatedEvent event) {  
        commandGateway.send(new ConfirmStudentEnrollmentCommand(/* ... */));  
    }  
  
}
```
## Caveats
- Sagas need a no args constructor 
- Multiple Sagas can exist, each associated with a specific aggregate (via associationProperty)
	- meaning for a lectureId="1" a different saga is invoked than for lectureId="2"
- Sagas are long-running
- Sagas can have multiple @EndSaga conditions 
- Can also end sagas via `SagaLifecycle.end()`
- Injected resources that are stored in fields of the Saga must have the `transient` keyword 
## Links 
- Saga implementation https://docs.axoniq.io/axon-framework-reference/4.11/sagas/implementation
- [[DDD AXON Digital Restaurant Demo]] example: https://github.com/idugalic/digital-restaurant/blob/920248e62c5b7d9b8d3c365b2f911355aa19c7db/drestaurant-libs/drestaurant-courier/src/main/kotlin/com/drestaurant/courier/domain/CourierOrderSaga.kt
- ProcessOrderSaga: https://github.com/AxonIQ/code-samples/blob/main/saga/src/main/java/io/axoniq/dev/samples/saga/ProcessOrderSaga.java
