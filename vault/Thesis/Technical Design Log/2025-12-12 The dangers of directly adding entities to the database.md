I just had a test fail in my ES-CQRS app that passed in the CRUD app, even though the business logic was the same.
Problem:
- CRUD-seed=directly add entities, bypass service logic 
- ES-CQRS-seed=publish _commands_ which pass through the aggregate 

I dont't seed data the same way. I could also send _events_ to the system instead of commands.

