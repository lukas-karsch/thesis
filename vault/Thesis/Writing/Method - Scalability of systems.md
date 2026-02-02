Since you’re comparing **Event Sourcing (ES)** with **CRUD + Audit Logs**, your scalability section needs to move beyond just "adding more servers." You need to address how the underlying data models handle growth in volume, concurrency, and complexity.

Kleppmann’s _Designing Data-Intensive Applications_ is the gold standard here—it will be your primary source for concepts like partitioning and derived data.
## 1. Dimensionality of Scalability
Before comparing the two architectures, define _what_ is scaling.
- **Throughput Scalability:** How the system handles an increasing number of commands (writes) vs. queries (reads).
- **Data Volume:** How the system behaves as the history of events or audit logs grows into the terabytes.
- **Organizational Scalability:** How easily multiple teams can work on the system without creating bottlenecks (the "microservices" angle).
## 2. Write Scalability (The Bottleneck)
This is where the two architectures diverge significantly
- **CRUD Contention:** Discuss **Locking and Concurrency Control**. In CRUD, scaling writes often leads to database contention on specific rows.
- **ES Append-Only Writes:** ES scales writes by treating them as immutable additions. Discuss how this avoids update locks but introduces the need for **Partitioning (Sharding)** based on Aggregate IDs.
- **The "Audit Log" Overhead:** In a CRUD system, an independent audit log often requires a **Two-Phase Commit (2PC)** or an Outbox Pattern to ensure the log and the state stay in sync. Explain how this affects write latency.
## 3. Read Scalability & Polyglot Persistence
- **Command Query Responsibility Segregation (CQRS):** This is essential for ES. Explain how scaling reads in ES involves "projecting" events into read-optimized views.
- **Materialized Views:** Compare how CRUD uses indexes vs. how ES uses asynchronous projections to scale complex queries.
- **Eventual Consistency:** Address the trade-off. Scaling reads via projections often means accepting a lag in data visibility.
## 4. Resource Consumption & Maintenance
- **Storage Growth:** ES stores every state change. Discuss **Snapshots** as a scaling strategy to prevent the system from having to replay millions of events to rebuild current state.
- **Replay Scalability:** How long does it take to "re-sync" a new microservice? This is a unique scalability challenge in ES that doesn't exist in CRUD.
## 5. Traceability as a Scaling Factor
- **Audit Log Performance:** In CRUD, an audit log is often a "sidecar" that grows linearly.
- **Intrinsic Traceability:** In ES, the log _is_ the state. Discuss how "scaling" traceability in ES is "free" because the data is already in that format, whereas in CRUD, it requires additional indexing and storage overhead.

---
### Recommended Literature
- **"Designing Data-Intensive Applications" (Martin Kleppmann):** Focus on Chapter 1 (Reliability, Scalability, Maintainability) and Chapter 11 (Stream Processing).
- **"Building Microservices" (Sam Newman):** Excellent for the "Organizational Scalability" and service decomposition aspects.
- **"Domain-Driven Design" (Eric Evans):** Essential for understanding **Aggregates**, which are the unit of scalability in Event Sourcing.
- **"Implementing Domain-Driven Design" (Vaughn Vernon):** Specifically the chapters on Event Sourcing and CQRS for practical architectural patterns.
- **Academic Paper:** _"Event Sourcing: Statements and Replies"_ by Dirk Riehle. It provides a formal look at the trade-offs of the pattern.
