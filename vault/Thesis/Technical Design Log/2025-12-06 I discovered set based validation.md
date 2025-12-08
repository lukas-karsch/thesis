See [[Axon Validation]] - set based validation is a technique described in this blog post by axoniq: https://www.axoniq.io/blog/2020set-based-consistency-validation

Basically, its possible to create lookup tables that do not belong to the _query_ side, but to the _command_ side. They can be made immediately consistent using @ProcessingGroup and can therefore safely be used in validation. 
## Subscribing processors 
https://docs.axoniq.io/axon-framework-reference/4.11/events/event-processors/subscribing/
