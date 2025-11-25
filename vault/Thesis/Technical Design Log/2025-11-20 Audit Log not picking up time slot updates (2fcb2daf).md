The @PreUpdate method in my audit listener does NOT pick up changes made to the timeSlots() set. This is because the lectures table actually has no direct reference to the timeSlot table. 
## Possible solution
Adding a `version` field to the lecture - that is automatically incremented when i make changes, and the changes should be picked up in the audit log.

Actually, this version field makes sense to use in my project anyways, as it is used for optimistic concurrency control. 

-> this solution was implemented and works. 
## N+1 for snapshot JSON 
#todo
@PostLoad creates a JSON snapshot of the entities. While this uses my custom object mapper that only serializes IDs of relationships, value objects like the time slots of lectures will be fetched... 
This might be a big performance bottleneck - have to check! 
## When creating a new entity, I cant see its ID 
- @PrePersist = not yet on the database
-  but ID is generated on the database 
- Maybe use @PostPersist? But that will kick off a new query _after_ saving
Solved here: [[2025-11-24 Audit Log - include ID (7ccd99be)]]
## When updating, FK fields are null 
Solution: instead of accessing the "id" field on entities, call the getId() method. 
Why this works? Hibernate generates a proxy for lazy loaded relationships - the relationship object exists on the entity, but its empty. Calling the getter means fetching the data so it can be inserted. 
**This is a N+1 query**
- could add a `courseId` (read-only) field on `Lecture` mapped to the FK column.
- but thats not generic.. 