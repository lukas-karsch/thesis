I was thinking of replacing "scalability" with "flexibility".
## Scalability
What would that include? 
1. **Resource consumption (efficiency)**
   Assume scalability is a function of efficiency - more CPU or RAM overhead, Disk I/O will require twice the hardware to scale to the same level 
2. **Database Growth, Index performance**
   Test read performance at 10k and 1M records, e.g. for SumAllCredits - does it slow down linearly in one application, but not in the other (e.g. if the projection is updated by incrementing the previous count)? 
3. **Contention / Lock analysis**
   Pessimistic locking in CRUD overhead? Compare maximum throughput on write operations 
   -> increase VUs 
4. **Historical State:**
   Event replay not scalable without Snapshots -> have to configure and include that in the thesis 
Careful: Audit Log implementation might skew results. Projections are async -> reading is fast, but may be stale 
## Flexibility
What i was thinking first: create a metric which scores ease of development, changes to existing code, any introduced dependencies or coupling (less is better), time to implement. 
-> wanted to score this in numbers so it gets more comparable 
-> but probably still bad practice 
