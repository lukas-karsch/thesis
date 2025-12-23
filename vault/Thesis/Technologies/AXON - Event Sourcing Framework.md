Access the Axon Dashboard: http://localhost:8024/

Serializer configuration: https://docs.axoniq.io/axon-framework-reference/4.11/serialization/
Message intercepting: https://docs.axoniq.io/axon-framework-reference/4.11/messaging-concepts/message-intercepting/#command-handler-interceptors
Building an Axon application from scratch: https://docs.axoniq.io/bikerental-demo/main/
## Accessing Aggregates 
Can inject an `EventSourcingRepository<T>` to access aggregates. This will replay events and give access to version-controlled aggregates (that can throw `ConcurrencyException`)
## Axon Server Dashboard 
![[Pasted image 20251223012238.png]]
Access event payloads, commands etc. Available at `localhost:8024` or at the testcontainer (check SpringBoot logs)
## Links 
- [[CQRS (Command Query Responsibility Segregation)]]
- [[AXON Subscription Queries]]
- https://academy.axoniq.io/
- Axon docs: https://docs.axoniq.io/reference-guide/
- Axon quick start guide on YT https://www.youtube.com/playlist?list=PL4O1nDpoa5KQkkApGXjKi3rzUW3II5pjm
- Axon demo project https://github.com/AxonIQ/hotel-demo
- https://www.baeldung.com/axon-cqrs-event-sourcing
- Event Sourcing https://www.axoniq.io/concepts/event-sourcing
- Replaying events with an event processor: https://docs.axoniq.io/axon-framework-reference/4.12/events/event-processors/
- https://www.youtube.com/watch?v=v4Jt2-Vx2E4
- Creating aggregates from another aggregate: https://docs.axoniq.io/axon-framework-reference/4.11/axon-framework-commands/modeling/aggregate-creation-from-another-aggregate/
  i could use this to create lectures from courses etc 
- Demo project https://github.com/idugalic/digital-restaurant

