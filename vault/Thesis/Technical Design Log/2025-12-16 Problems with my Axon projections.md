- Not idempotent 
- race conditions, e.g:
  ```java
	  var entity = studentLecturesRepository.findById(e.studentId())  
	        .orElseGet(() -> {  
	            var newEntity = new StudentLecturesProjectionEntity();  
	            newEntity.setId(e.studentId());  
	            return newEntity;  
	        });
	  ```
-> lost update 
- querying local projections is bad practice
## Potential fixes
- use tracking processors with segmenting by studentId (or other applicable)
	- i use tracking, except for tests... 
	- but have to look into segmenting 
	- would have to configure every event processor 
- first insert empty entity, then update that 
- normalized tables would also enable atomic inserts... but i chose denormalization on purpose 
	- e.g. lost update when updating a serialized list of entities 
- every projection needs to keep track of events it cares about! 

https://chatgpt.com/c/6941703b-85c0-8329-8a25-c83b59c8092b