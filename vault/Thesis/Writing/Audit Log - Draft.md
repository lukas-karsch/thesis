Here, i talk about different ways to implement an audit log / change tracking. Simple manual logging, automated using a change tracker on JPA, database triggers or further technologies (complex change tracking mechanism).

I explain why I made the choice to use a JPA entity listener. I describe the basic structure of the AuditLogEntry as introduced by Fowler. Then i show how it works: AuditableEntity superclass, listener which listens for changes and serializes them. Add how capturing business intent is possible by adding context; but that is also a secondary mechanism and prone to be forgotten by devs.
files: AuditLogEntry, AuditEntityListener, IdPropertySerializerModifier 

Then, go into the details: how to serialize relationships (FK), especially nested (course <-> prerequisites, recursive danger).

Maybe, here I can describe how simple and complex state reconstruction works / could work.
