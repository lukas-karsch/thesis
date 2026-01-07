This file contains my current outline, always up to date. It was created by looking at my exposé and the "Bachelorarbeit Vorlage" by Heuzeroth
## Notes 
- orientieren an https://www.dirk-heuzeroth.de/forschung-und-lehre/#theses-types (Messungs-Thesis) und [[BachelorarbeitVorlage.pdf]]
---
## Ehrenwörtliche Erklärung
## 1. Table of contents 
## 2. Introduction
### 2.1 Motivation
### 2.2 Research question(s)
> How does an Event Sourcing architecture compare to CRUD systems with an independent audit log, when it comes to scalability, performance and traceability?
### 2.3 Goals and non goals 
### 2.4 Structure of the paper  
- every chapter, short description of contents 
## 3. Basics
### 3.1 Web APIs 
http, (stateless) communication, the term "API", REST principles, CRUD
### 3.2 Layered Architecture Foundations
- layered architecture
- anemic data model 
### 3.3 DDD Architectural Foundations 
- domain driven design
- rich domain model 
- what is aspect driven design?
- differences to CRUD 
- vertical slice, screaming architecture, and more architecture styles
### 3.4 Event Sourcing and event-driven architectures
- Formal definition
- event stream as single source of truth 
### 3.5 CRUD architecture 
**Characteristics:**
- Same objects/services handle **reads and writes**
- Same data model for:
    - Commands (`create/update/delete`)
    - Queries (`read`)
- Typically exposed via REST:
    - `GET /users`
    - `POST /users`
    - `PUT /users/{id}`
- Direct mapping between database tables and domain objects
- ACID 
### 3.6 CQRS Architecture
- BASE 
- separation of reads and writes 
### 3.7 (Eventual) Consistency 
- differences in CRUD and CQRS 
- ACID vs. BASE
- (maybe: concurrency control / optimistic vs pessimistic locking)
### 3.8 Traceability and auditing in IT systems 
#### 3.8.1 Why is traceability a business requirement 
#### 3.8.2 Audit Logs 
#### 3.8.3 Event Streams 
#### 3.8.4 Rebuilding state from an audit log and an event stream
### 3.9 Scalability of systems 
-> Only if i plan to do the database scaling
- Different ways to scale systems
- How to scale reads and writes (in general and specific to event sourced systems)
## 4. Related Work 
## 5. Proposed Method 
### 5.1 Project requirements
- differentiate between functional and non-functional requirements 
- shared API and tests 
- tag requirements with IDs to reference them later 
- show tradeoff when using an identical API -> have to use sendAndWait to be synchronous 
### 5.2 Performance measurement 
-> meant to answer the "performance" part of my research question 
Benchmarking via load testing. Time taken per request (Median, P95); CPU utilization, database IO; Database size for a fixed number of operations ... 
### 5.3 Scalability or flexibility (TODO)
-> meant to answer the "scalability" part of my research question 
- measure db size, IO; suggest ways to scale 
#todo **QUESTION**: replace with "flexibility"? 
- how easily are new features added? 
- could measure: 
	- new code 
	- time to implement 
	- interference with existing code / data structures 
### 5.4 Traceability 
-> meant to answer the "traceability " part of my research question 
### 5.5 Tech Stack
- explain choices 
- show how i tried to avoid bias in technologies 
- measurements with k6 
## 6. Implementation
### 6.1 CRUD implementation
- Relational Modeling 
- Audit log implementation
### 6.2 ES/CQRS implementation 
- aggregates 
- event modeling / data flows 
- show example for one flow of data from controller via command handler to synchronous (write-side) and tracking (read-side) projectors 
### 6.3 Infrastructure
- Testcontainers 
- Dockerfiles, docker-compose.yml 
- (if applicable) deployment to VM 
## 7. Results 
**Important:** no assessment here! Only show data 
- **Performance Benchmarking:** Raw data for write/read latency (P95, Median).
- **Scalability Analysis:** Database growth and resource utilization.
- **Traceability Qualitative/Quantitative Analysis:** Replaying events vs. reconstructing from Audit Logs.
## 8. Discussion  
### 8.1 Analysis of results 
- interpret results 
- how valid are the results?  
- show trade-offs between approaches 
- who might benefit from which approach? 
- assess flexibility 
### 8.2 Conclusion & Further work 
- what could be missing from my implementations 
- what could be missing from my measurements and test strategies? 