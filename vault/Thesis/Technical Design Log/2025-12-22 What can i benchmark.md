#performance 
# Things to measure / performance test
### Writes 
- Simple writes (no checks at all, e.g. creating a course withouot prerequisites)
- Dependent writes (writes that require one or more check(s), e.g. creating a lecture or creating a course)
- Complex writes (check prerequisites, check credits, etc.)
### Reads 
- Simple reads (course by ID)
- Join reads (which require JOINs in the CRUD system, e.g. grades, credits)
## Write then read 
- "Read your writes"
	- what does it mean?
	- relevant here? 
	- how to achieve? 
- write, measure latency until projections are updated 
## Important metrics 
- median time 
- avg. time 
- max time 
- min time 
- time to consistency vs. response time 
	- measure time to consistency under differing loads and demonstrate how / if the time increases under more stress on the system 
## Plan
Performance testing plan inside `/plan`
