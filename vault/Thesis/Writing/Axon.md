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

- Command Handlers 
- Sagas 
- Projectors / Projections 
- Subscription Query 
- Example flow? Or is the concrete flow enough? 