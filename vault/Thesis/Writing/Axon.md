Axon Framework is an open-source Java framework for building event-driven appli-
cations. Following the CQRS and event-sourcing pattern, Commands, Events and
Queries are the three core message types any Axon application is centered around.
Commands are used to describe an intent to change the application's state. Events
communicate a change that happened in the application. Queries are used to re-
quest information from the application.

Axon also supports Domain Driven Design by providing tools to manage entities
and domain logic

Axon Server is a platform designed specifically for event-driven systems. It
functions as both a high-performance Event Store and a dedicated Message Router
for commands, queries, and events. By bundling these responsibilities into a sin-
gle service, Axon Server replaces the need for separate infrastructures such as a
relational database for events and a message broker like Kafka or RabbitMQ for
communication. Axon Server is designed to seamlessly integrate with Axon Frame-
work. When using the Axon Server Connector, the application automatically finds
and connects to the Axon Server. It is then possible to use the Axon server without
further configuration. 

## Command Handling
Command dispatching is the starting point for handling a command message in Axon. Axon handles commands by routing them to the appropriate command handler. The command dispatching infrastructure can be interacted with using the low-level `CommandBus` and a more convenient `CommandGateway` which is a wrapper around the `CommandBus`. 

`CommandBus` is the infrastructure mechanism responsible for finding and invoking the correct command handler. At most one handler is invoked for each command; if no handler is found, an exception is thrown. 

Using `CommandGateway` simplifies command dispatching by hiding the manual creation of `CommandMessages`. The gateway offers two main methods for synchronous and asynchronous patterns. The `send` method returns a `CompletableFuture`, which is an asynchronous mechanism in Java. If the thread needs to wait for the command result, the `sendAndWait` method can be used. 

In general, a handled command returns `null`, if handling was successful. Otherwise a `CommandExecutionException` is propagated to the caller. While returning values from a command handler is not forbidden, it is used sparsely as it contradicts with CQRS semantics. One exception: command handlers which _create_ an aggregate typically return the aggregate identifier. 
## Query Handling
Before a query is handled, Axon dispatches it through its messaging infrastructure. Just like the command infrastructure, Axon offers a low-level `QueryBus` which requires manual query message creation and a more high-level `QueryGateway`. 

In contrast to command handling, multiple query handlers can be invoked for a given query. When dispatching a query, callers can choose whether they want a single result or results from all handlers. When no query handler is found, an exception is thrown. 

The `QueryGateway` includes different dispatching methods. For regular "point-to-point" queries, the `query` method can be used. Subscription queries are queries where callers expect an initial result and continuous updates as data changes. These queries work well with reactive programming. For large result sets, streaming queries should be used. The response returned by the query handler is split into chunks and streamed back to the caller. 
All query methods are asynchronous by nature and return Java's `CompletableFuture`.
## Aggregates 
An aggregate is a core concept of Domain-Driven Design (DDD). In Axon, an aggregate defines a consistency boundary around domain state and encapsulates business logic. Aggregates are the primary place where domain invariants are enforced and where commands that intend to change domain state are handled.

Aggregates define command handlers using methods or constructors annotated with `@CommandHandler`. These handlers receive commands and decide whether they are valid according to domain rules. If a command is accepted, the aggregate emits one or more domain events describing _what_ happened. Command handlers are responsible only for decision-making; they must not directly mutate the aggregate’s state. Instead, all state changes must occur as a result of applying events.

Every aggregate is typically annotated with `@Aggregate` and must declare exactly one field annotated with `@AggregateIdentifier`. This identifier uniquely identifies the aggregate instance. Axon uses it to route incoming commands to the correct aggregate and to load the corresponding event stream when rebuilding aggregate state.

By default, Axon uses event-sourced aggregates. This means that aggregates are not persisted as a snapshot of their fields. Instead, their current state is reconstructed by replaying all previously stored events. Methods annotated with `@EventSourcingHandler` are called by Axon during this replay process to update the aggregate’s internal state based on event data. Since events represent facts that already occurred, event sourcing handlers must not contain business logic or make decisions.

Aggregates must not hold direct object references to other aggregates, as this would violate aggregate boundaries and transactional consistency. If an association to another aggregate is required, only its identifier may be stored.

Axon also supports multi-entity aggregates. In this model, an aggregate may contain child entities that participate in command handling. Such entities are registered using `@AggregateMember`, and each entity must define a unique identifier annotated with `@EntityId`. Based on this identifier, Axon is able to route commands to the correct entity instance within the aggregate.
## External Command Handlers 
Often, command handling functions are placed directly inside the aggregate. However, this is not required and in some cases it may not be desirable or possible to directly route a command to an aggregate. Thus, any object can be used as a command handler by including methods annotated with `@CommandHandler`. One instance of this command handling object will be responsible for handling _all_ commands of the command types it declares in its methods. 

In these external command handlers, aggregates can be loaded manually from Axon's repositories using the aggregate's ID. Afterwards, the `execute` function can be used to execute commands on the loaded aggregate. 
## Events
Axon's `EventBus` is the infrastructure mechanism dispatching events to the subscribed event handlers. Event stores offer these functionalities and additionally persist and retrieve published events. 

Event handlers are methods annotated with `@EventHandler` which react to occurrences within the app by handling Axon's event messages. Each event handler specifies the types of events it is interested in. When no handler for a given event type exists in the application, the event is ignored. 
## Sagas 
In Axon, Sagas are long-running, stateful event handlers which not just react to events, but instead manage and coordinate business transactions. For each transaction being managed, one instance of a Saga exists. A Saga, which is a class annotated with `@Saga` has a lifecycle that is started by a specific event when a method annotated with `@StartSaga` is executed. The lifecycle may be ended when a method annotated with `@EndSaga` is executed; or conditionally using `SagaLifecycle.end()`. A Saga usually has a clear starting point, but may have many different ways for it to end. Each event handling method in a Saga must additionally have the `@SagaEventHandler` annotation. 

The way Sagas manage business transactions is by sending commands upon receiving events. They can be used when workflows across several aggregates should be implemented; or to handle long-running processes that may span over any amount of time. For example, the lifecycle of an order, from being processed, to being shipped and paid, is a process that usually takes multiple days. A use case like this is typically implemented using Sagas. 

A Saga is associated with one or more association values, which are key-value pairs used to route events to the correct Saga instance. A `@StartSaga` method together with the `@SagaEventHandler(associationProperty="aggregateId")` automatically associates the Saga with that identifier. Additional associations can be made programmatically, by calling `SagaLifecycle.associateWith()`. Any matching events are then routed to the Saga. 

For example, a Saga managing an order's lifecycle may be started by an `@OrderPlaced` event and associated with the `orderId`. It can then issue a `CreateInvoiceCommand` using an `invoiceId` generated inside of the event handler. It then associates itself with this ID to be notified of further events regarding this invoice, such as an `InvoicePaidEvent`. 

- Projectors / Projections 
- Subscription Query 
- Example flow? Or is the concrete flow enough? 