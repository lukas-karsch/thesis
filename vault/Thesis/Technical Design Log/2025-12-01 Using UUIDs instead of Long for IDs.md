To implement the CQRS app, I need to first migrate every entity to use UUIDs instead of Longs. The reason being, that in CQRS, the ID of an aggregate _must_ be created inside the application layer. However, that does not work when using Longs: the generated ID must be unique and random. 
## UUIDs
UUIDs are random, 128-bit Strings.
### UUIDv7
Using UUIDv7 has real benefits: because the first bits encode a timestamp (milliseconds since epoch), the UUIDs are roughly monotonic in creation time — so indexes on them (e.g. primary key B-trees) enjoy better locality, reduced fragmentation, and more efficient inserts/reads compared to completely random UUIDs. 
## Generating a UUID 
Generating UUIDv7 (newest, ordered) and using it in an annotation: 
https://manbunder.medium.com/streamline-uuid-v7-generation-in-spring-boot-entities-with-custom-annotations-hibernate-6-5-4ddc018895cf

I will employ this strategy ^

First, change all DTOs and controller interfaces. I will also make sure to modify the controllers so that they return the created ID 
## Fun side effect on tests
I just had some tests fail after making the changes. Reason: I used the wrong IDs when making requests, e.g. using the studentId as parameter for lectureId. This often worked because both IDs were "1" - now, with UUIDs, the error was exposed immediately. 