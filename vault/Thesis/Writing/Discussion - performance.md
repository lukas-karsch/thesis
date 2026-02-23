mention increased latency as soon as database and event store live on the network (in interpretation!
- especially for ES-CQRS app; because data travels from server -> event store -> server for projection processing -> postgres 
lookup projectors can be used outside of aggregates (message interceptor) to avoid blocking them while lookups are in progress -> only call aggregate once external validation is finished 
## What there would be to do to diagnose issues
- Profiling 
	- e.g. JSON serialization overhead for es-cqrs app
## CPU Usage 
Axon server and springboot share same CPU -> springboot usage shows "only" 60%, but axon server might take the rest -> in total, the system is overwhelmed. Should separate each service for better measurements. 
## Database connections 
- Why median of 0? 
### Get grade history
Why does the ES-CQRS application have such a high DB usage, when it should only look up the ID of the enrollment? There's indexes and no actual data is being fetched... 
