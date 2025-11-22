When fetching an object with @EntityGraph and loading associated collections, the nested objects do not get their own `snapshotJson` - `@PostLoad` is only called for the actual entity that was fetched, not for every entity that gets fetched using an EntityGraph eager collection loading.
This means that updating a nested object when it was loaded from an associated entity delivers a faulty audit log. 
## Can this be fixed? 
- traverse the entity graph and call setSnapshotJson for each entity
- terrible for performance - will kick off lots of additional queries 
	- only call `setSnapshotJson` when the entity is already fully loaded? 
	- not sure if possible - possible to do an instanceof check on a proxy class that is used by hibernate? 
	- probably would have to use reflection 
## "Solution"
- looks like the snapshotJson **IS** indeed set for entities, even if they were loaded via associations. 
- The problem: my `@PreRemove` function did not actually set `oldJson` - simple bug, but that was of course (once again) not picked up by an LLM 
- Glad this one only took me 5 minutes to figure out. 