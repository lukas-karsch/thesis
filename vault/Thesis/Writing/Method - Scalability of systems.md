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
--- 
## Correlation of latency, resource consumption and scalability 
### Jogalekar & Woodside, Evaluating the scalability of distributed systems 
- mathematical formula for scalability 
- response time is a variable in this formula 
$k_i$ = scale 
$\lambda(k)$  = throughput in responses / sec
$f(k)$ = average _value_ of each response, 
$C(k)$ = cost expressed as running cost per second to be uniform with $\lambda$

Productitity $F(k)$:
$F (k) = \lambda(k) * f(k) / C(k)$
This formula relates systems at two different scale factors; its a ratio of the productivites:
$\psi(k_1, k_2) = F(k_2) / F(k_1)$ 

A threshold for $ψ$ is set below which a system is deemed scalable 

**My value function could be:** 
$f(k) = 1 / (1 + (T(k) / \hat T))$
with $T(k)$ being the measured $latency_p95$ and $\hat T$ being 100ms (latency SLO)

**My cost function?**
- use resource utilization = cloud costs for servers, database, event store
	- could look at what my VMs have, then think scaling = More instances 
	- base this on CPU usage 
	- Show separate price for event store 
- $C(k) = C_{infra}(k) + C_{storage}(k) + C_{ops}(k)$$
	- infra = compute, cost of RAM / CPU per second 
- Cost function should include CPU and RAM usage 
	- database and event store are not included here. 
## Kleppman - designing data intensive systems 
- practical architectural mechanics of how resource saturation creates latency 
- when the system is saturated (e.g. CPU cores are maxed out), latencies skyrocket because e.g. queueing delays 
## Synthesis 
Learnings 
1. As latency increases, calculated scalability decreases. Maintaining a latency near $\hat T$ is essential for a system to be considered scalable -> source = function 
2. Resource consumption correlates with throughput. UNTIL the point where saturation is reached. At that point, locking starts. 
	1. cpu usage 
	2. garbage collection 
	3. database connections 
3. As soon as scalability comes with more overhead (coordination, locking), scalability decreases (Jogalekar, p. 22)
4. High resource consumption directly causes latency to increase (Kleppmann, p. 15,16)
--- 
## The Art of scalability
Chapter 12: Exploring architectural principles 
	- AKF's 12 architectural principles 
	- scale out not up 
Chapter 17: Performance and stress testing
	performance and stress testing for scalability 
Chapter 23: Splitting applications for scale 
Chapter 24: Splitting Databases for scale 
Chapter 26: Asynchronous design for scale 
