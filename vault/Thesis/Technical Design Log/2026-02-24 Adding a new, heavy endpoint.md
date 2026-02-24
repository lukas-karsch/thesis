`GET /lectures/all`
returns all lectures. 

Performance of **CRUD** with 50 VUs, 50 courses, 15 students: 
![[Pasted image 20260224163339.png]]
20 courses:
![[Pasted image 20260224163827.png]]
This makes no sense but ok 

--- 

Performance of **ES-CQRS** with 50 users, 50 courses, 15 students: 

![[Pasted image 20260224162945.png]]
20 Courses:
![[Pasted image 20260224164305.png]]

## Decision
I will run the test with 25 courses. 
