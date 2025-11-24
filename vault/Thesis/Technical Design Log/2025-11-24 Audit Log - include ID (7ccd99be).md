https://chatgpt.com/c/69246e1f-80f0-832e-8ff1-84a4d651d1bd
I'm currently working on the historic grades query. For this, I need to query the audit log. The problem that I was avoiding for some time: the ID is not persisted when creating the entity, because of the @PrePersist annotation (ID is generated db-side) 
## Possible solutions
- Let the application set an ID
	- does not work when using identity (incrementing Long)
	- would have to use UUIDs -> refactoring necessary 
- Use @PostPersist 
	- Audit log entry is now not part of the same transaction (?)
		- can be controlled: https://docs.spring.io/spring-framework/docs/3.2.5.RELEASE/javadoc-api/org/springframework/transaction/support/TransactionSynchronization.html
	- lifecycle problems 
- raise an application-level event: `@DomainEventPublisher`
## Decision
Use `@PostPersist` with TransactionSynchronizationManager. Using this, I can make sure the audit log gets access to the entity's ID; and the auditing runs in the same transaction. 