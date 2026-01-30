Option for Axon Server:

| Option                               | Description                    | Default |
| ------------------------------------ | ------------------------------ | ------- |
| axoniq.axonserver.event.segment-size | Size for new storage segments. | 256MB   |
https://docs.axoniq.io/axon-server-reference/development/axon-server/administration/admin-configuration/configuration/
---
-> for environment variable, turn it into this: 
AXONIQ_AXONSERVER_EVENT_SEGMENT-SIZE=4MB
## Why change to 4MB? 
I measure Event Store size in my metrics, but a size of 256MB as default segment is really big.  To see changes and actually be able to measure event store size, I change the default to something smaller. This likely impacts performance, even though im not even sure 4 MB gets full quickly.

Might have to introduce a dedicated test to fill the event store. 