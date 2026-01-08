# Event Sourcing vs. CRUD

This is the source code to Lukas Karsch's bachelor's thesis, written during the winter semester 2025 / 2026 under
supervision of Prof. Dr. Tobias Jordine and Felix Messner.

The research question for the thesis is

> How does an Event Sourcing architecture compare to CRUD systems with an independent audit log, when it comes to
> scalability, performance and traceability?

## About the project

This thesis compares two implementations of an API with the same interface - one implementation is built using a typical
layered CRUD architecture, the other one is built using DDD principles and Event Sourcing / Command Query Responsibility
Segregation.

Both implementations use the same underlying technologies:

- Backend runs on SpringBoot
- Database powered by PostgreSQL

## Tech Stack

- SpringBoot
- Postgres
- JPA
- Lombok
- Testcontainers

### ES-CQRS App

The es-cqrs app additionally uses the Axon server and the Axon framework for command handling, event sourcing and CQRS.

## Get started

### Dive into the code:

To examine the API definitions / interfaces, look [here](api/src/main/java/karsch/lukas)

### Running the applications

You need an instance of Docker running. When the application is started, you can test the API endpoints
using [Swagger UI](http://localhost:8080/swagger-ui/index.html)

#### Running using testcontainers

Both applications can be run using testcontainers. Starting a "test run" from the files linked below will spin up the
necessary testcontainers. Keep in mind that data is not persisted in between runs.

[This](impl-crud/src/test/java/karsch/lukas/TestCrudApplication.java) is a good starting point to get the `impl-crud`
application running.

[This](impl-es-cqrs/src/test/java/karsch/lukas/TestEsCqrsApplication.java) is where the `es-cqrs` app can be started.

#### Running with docker-compose

The applications can also be started using `docker compose`. When using IntelliJ, the apps can be started using the
predefined run configurations.

Otherwise, here are step-by-step instructions:

1. Open a terminal in the root of the project and build the modules
   ```bash
   mvn clean package -DskipTests
   ```
2. Build the docker image:
   ```bash
   cd impl-crud
   docker build -t lkarsch/impl-crud-docker .
   ```
3. Run docker compose
   ```bash
   cd ..
   docker-compose up -d crud-app # "es-cqrs-app" | "crud-app"
   ```

- **crud-app** will start at port **8080**
- **es-cqrs-app** will start at port **8081**

## Technical Documentation, Architectural Decisions

`vault` contains the entire documentation and notes that I wrote during my work on the thesis. Potentially interesting
architectural desicions are documented [here](vault/Thesis/Technical%20Design%20Log). Best viewed inside
of [Obsidian](https://obsidian.md/).

## LaTeX

The latex file for the thesis can be found [here](latex/main.tex).

## Further Links

- https://spring.io/projects/spring-boot
- https://www.postgresql.org/
- https://www.axoniq.io/ 
