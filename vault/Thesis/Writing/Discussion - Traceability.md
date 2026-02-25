- ES-CQRS is not sufficient when trying to audit read operations. 
- ES-CQRS: Events are explicit. They DID happen. Even later changes in business logic do not change what happened. Later "changes" are only possible by emitting new events. 
### Efficiency 
- assumption: fetching data from audit log tables may be more efficient, because date filters can be used on indexed tables
- ES system has to play ALL events. (Snapshots can not be used when replaying events, they are only used when rehydrating aggregates. $\rightarrow$ this part probably belongs in result)
- Results: the "reconstruct grade history" load test proved this. CRUD was a lot faster here. 