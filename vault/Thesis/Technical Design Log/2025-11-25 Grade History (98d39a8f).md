#audit-log 
To get the grade history, I look at audit log entries. Extracting the grade is quite simple; I can parse the json and get the "grade" field:
```java
String newJson = auditLogEntry.getNewValueJson();  
try {  
    return mapper  
            .readTree(newJson)  
            .get("grade")  
            .asInt();
 } //...
```
This works well and is definitely enough for that case. But as soon as I need to inspect relationships / nested elements, it gets trickier. To save performance and avoid recursive serialization, I only store IDs of related objects. The problem here is getting those objects deserialized, to the exact state that they were in the specific point of time. 

I don't have to worry about this right now, but maybe I can create a business requirement that needs this. Then I could show how complex and time-consuming the algorithm is - while implementing and also at runtime.
## Algorithm 
Without putting too much though into it, the algorithm would have to look something like this: 
1. Get audit log entries for the entity 
2. Create a new instance of the entity class; set all fields 
3. For every field that only includes an ID (how would I even recognize it's an ID vs. an int field in the entity?): back to #1 with the entity ID

-> Recursion 
-> Potentially lots of queries 
-> Bug-prone (have to "detect" whether a field is a relationship or an int)
	-> can use reflection here on the original entity to check type of the field. 
## Decision
- Think about a business requirement that would make this necessary 
- Do not implement an algorithm for now 
## Links 
- [[2025-11-14 Audit Log (c5367a6b)]]
- adding the ID to make this possible: [[2025-11-24 Audit Log - include ID (7ccd99be)]]
