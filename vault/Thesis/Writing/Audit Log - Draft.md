# Audit Log
An audit log is a critical component for enterprise applications, serving multiple purposes: it provides accountability by tracking who changed what and when, supports debugging by reconstructing the history of an entity, and often fulfills legal or compliance requirements (needs reference).
## Implementation Approaches
There are several strategies to implement an audit log, each with its own trade-offs:
1.  **Manual Logging**: Developers explicitly call a logging service in every service method that modifies data. While simple, this can lead to code duplication is prone to human error, such as developers forgetting to add a log statement. A code example might look like this: 
	```java
	public void updatePhoneNumber(User user, int newNumber) {
		logChange(Date.now(), user, user.getPhoneNumber(), newNumber);
		user.setPhoneNumber(newNumber);
	}
	
	void logChange(Date date, User user, Object oldValue, Object newValue) {
		LogEntry logEntry = new LogEntry(date, user, oldValue, newValue);
		logRepository.persist(logEntry);
	}
	```
2.  **Database Triggers**: Database-level triggers can capture changes automatically. This offers high performance and guarantees that no change is missed, even if made outside the application. However, it ties the logic to a specific database vendor (e.g., PostgreSQL or Oracle) and makes the business logic harder to maintain and test, as it resides outside the codebase (needs reference).
3.  **Hibernate Envers**: A popular solution for JPA-based applications. It automatically versions entities. However, Envers typically creates a shadow table for every entity, which can be overkill if only specific high-level changes need to be tracked, and querying the history can be complex. (needs reference)
4.  **JPA Entity Listeners**: Using the JPA specification's lifecycle events (`@PrePersist`, `@PreUpdate`, etc.) to intercept changes. This approach is database-independent and keeps the logic within the Java application, allowing access to Spring's security context. The security context makes it easy to access the current user, making it possible to attach them to the new audit log entry. (needs reference)
## Chosen Solution: JPA Entity Listener
For this project, I chose the **JPA Entity Listener** approach. It offers a good balance between automation and flexibility. It ensures that every change to an entity is captured without polluting the service layer with logging calls, while still allowing the application to enrich the log with application-level context (like the current user).
### Data Model
The audit log is stored in a single table, represented by the `AuditLogEntry` entity. This structure allows for easy querying of all system changes in chronological order. The entry contains:
-   **Entity Reference**: `entityName` and `entityId` (UUID) to identify the changed object.
-   **Metadata**: `timestamp`, `operation` (CREATE, UPDATE, DELETE), and `modifiedBy` (identifying the user or system process).
-   **State**: `oldValueJson` and `newValueJson` to store the serialized state of the entity before and after the change.
-   **Context**: `contextJson` to capture additional business intent (discussed below).
### The `AuditableEntity` and `AuditEntityListener`
To enable auditing, entities extend a base class, `AuditableEntity`. This class marks the entity with the `@EntityListeners(AuditEntityListener.class)` annotation and provides a transient field, `snapshotJson`, used to store the state of the entity when it is loaded from the database.

The core logic resides in `AuditEntityListener`. It hooks into the JPA lifecycle:
1.  **@PostLoad**: Immediately after an entity is fetched from the database, the listener serializes it to JSON and stores it in the `snapshotJson` field. This serves as the "old value" for any subsequent updates.
2.  **@PrePersist**: Before a new entity is saved, a log entry is created with the operation `CREATE`. The `newValueJson` is the current state; `oldValueJson` is null.
3.  **@PreUpdate**: Before an existing entity is updated, the listener compares the current state with the `snapshotJson`. It creates an `UPDATE` entry using `snapshotJson` as the `oldValue` and the current state as the `newValue`.
4.  **@PreRemove**: Before deletion, a `DELETE` entry is created, preserving the last known state.
### Serialization and Relationships
A major challenge in serializing JPA entities to JSON for an audit log is handling relationships. A naive JSON serialization would follow every relationship (e.g., `Lecture` -> `Course`), potentially serializing huge entity graphs or even causing a `StackOverflowError` due to cyclic references. One example for such a cyclic reference would be the self-reference of `CourseEntity`, which each course having a one to many relationship to prerequisite courses. 

A custom Jackson module, `IdSerializationModule`, was implemented to deal with the problem of relationship serialization. This module overrides the default serialization for related entities. Instead of serializing the full nested object, it only writes the related object's ID. This results in a flat, lightweight JSON structure that is easy to read and safe to store.
### Capturing Business Context
One downside of automatic auditing is that it captures *what* changed (e.g., "status changed from OPEN to CLOSED") but not necessarily *why* (e.g., "Course cancelled by Professor X due to illness"). To mitigate this, the implementation supports an `AuditContext`. Services can attach metadata to the current thread/transaction, which the `AuditEntityListener` retrieves and stores in the `contextJson` field. This bridges the gap between technical data changes and business intent.
## Original draft 
Here, i talk about different ways to implement an audit log / change tracking. Simple manual logging, automated using a change tracker on JPA, database triggers or further technologies (complex change tracking mechanism).
 
I explain why I made the choice to use a JPA entity listener. I describe the basic structure of the AuditLogEntry as introduced by Fowler. Then I show how it works: AuditableEntity superclass, listener which listens for changes and serializes them. Add how capturing business intent is possible by adding context; but that is also a secondary mechanism and prone to be forgotten by devs.

Then, go into the details: how to serialize relationships (FK), especially nested (course <-> prerequisites, recursive danger).

Maybe, here I can describe how simple and complex state reconstruction works / could work