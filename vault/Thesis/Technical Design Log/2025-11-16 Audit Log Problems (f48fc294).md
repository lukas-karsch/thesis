## Recursive serialization 
When entities have relationships, the audit log serializes those aswell.
With nested relationships, like on courses <-> prerequisites, this can lead to big problems
### > Only persist IDs of relationships 
- can i find out whether a field is a relationship? 
- Jackson has @@JsonIdentityInfo(  
        generator = ObjectIdGenerators.PropertyGenerator.class,  
        property = "id"  
	)
- find out if i can place this on the superclass
- otherwise, use jackson mixins or modules 
Using a module is probably best. 
-> implemented in `IdSerializationModule`
## Business intent 
The audit log is currently _designed_ to fail traceability requirements because it does not store "comments" or a business reason.

Maybe I should try to add that, however it is difficult to add a business intent to the automatic audit log implementation - would have to work via request context 
## Async operations 
I added the modified_by column to the `audit_log` table. That works, but for async work (which currently doesn't exist), the request context will be lost! 