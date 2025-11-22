# Event Sourcing vs. CRUD 

This is the source code to Lukas Karsch's bachelor's thesis, written in during the winter semester 2025 / 2026 under
supervision of Prof. Dr. Tobias Jordine and Felix Messner.

The research question for the thesis is

> How does an Event Sourcing architecture compare to CRUD systems with an independent audit log, when it comes to scalability, performance and traceability? 

## About the project 

This thesis compares two implementations of an API with the same interface - one implementation is built using a typical layered CRUD architecture, the other one is built using DDD principles and Event Sourcing. 

Both implementations use the same underlying technologies:

- Backend runs on SpringBoot
- Database powered by PostgreSQL

## Tech Stack 

### impl-crud

- SpringBoot
- Postgres
- JPA
- Flyway (DB migrations)
- Lombok
- Testcontainers

### impl-es-cqrs

TODO

## Get started

### Dive into the code:

To examine the API definitions / interfaces, look [here](api/src/main/java/karsch/lukas)

### Running the applications

You need an instance of Docker running.

[This](impl-crud/src/test/java/karsch/lukas/TestCrudApplication.java) is a good starting point to get the `impl-crud`
application running. It automatically starts a testcontainer with Postgres - be careful, your data is not persisted!

(TODO) for impl-es-cqrs

### Technical Documentation, Architectural Decisions

`vault` contains the entire documentation and notes that I wrote during my work on the thesis. Potentially interesting
architectural desicions are documented [here](vault/Thesis/Technical%20Design%20Log). Best viewed inside
of [Obsidian](https://obsidian.md/).

## Further Links

- https://spring.io/projects/spring-boot
- https://www.postgresql.org/
