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
## Gemini Query
I have an api and two applications which implement this api. one works with simple CRUD (postgres), the other relies on event sourcing and CQRS (using Axon). My goal is to compare them in performance and other topics. To prepare for performance testing, i dockerized my applications and set up k6 (@performance-tests/k6/). Now, I am thinking about which benchmarks to take, which metrics are important, which endpoints are relevant and interesting to compare the two implementations. I put some thoughts into @vault/Thesis/Technical\ Design\ Log/2025-12-22\ What\ can\ i\ benchmark.md . Please use my thoughts, do more research and look at the codebase / api to identify core topics and endpoints. then, create a file in @plan/ which outlines my testing strategy. Remember, this is for a bachelor's thesis. dont emphasize that fact in your document, but keep a scientific approach in mind.
## Plan
Plan inside `/plan`
