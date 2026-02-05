mention increased latency as soon as database and event store live on the network (in interpretation!
- especially for ES-CQRS app; because data travels from server -> event store -> server for projection processing -> postgres 
lookup projectors can be used outside of aggregates (message interceptor) to avoid blocking them while lookups are in progress -> only call aggregate once external validation is finished 
## What there would be to do to diagnose issues
- Profiling 
	- e.g. JSON serialization overhead for es-cqrs app
