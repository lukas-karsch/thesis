# Performance Benchmark Plan: CRUD vs. Event Sourcing/CQRS

## 1. Introduction

### 1.1. Objective

This document outlines a comprehensive performance testing strategy to benchmark and compare two backend implementations
of a university management system:

1. **`impl-crud`**: A traditional Create, Read, Update, Delete (CRUD) architecture using Spring Data JPA and a
   relational PostgreSQL database.
2. **`impl-es-cqrs`**: An architecture based on Event Sourcing (ES) and Command Query Responsibility Segregation (CQRS)
   using the Axon Framework.

The goal is to quantitatively analyze the performance characteristics, scalability, and trade-offs of each architectural
style under various workloads, providing empirical data for a bachelor's thesis.

### 1.2. Scope

The benchmark will focus on black-box testing of the REST API. The primary areas of investigation are:

- **Write Performance**: The system's ability to handle commands that create or modify data.
- **Read Performance**: The system's efficiency in querying data, from simple lookups to complex aggregations.
- **Eventual Consistency**: A specific characteristic of the ES-CQRS system, measuring the time delay for read models to
  be updated after a write.

## 2. Methodology

### 2.1. Tooling

- **Load Generation**: `k6` will be used to script and execute load tests against the application endpoints.
- **Containerization**: `Docker` and `Docker Compose` will ensure a consistent and isolated environment for each
  application and its dependencies (PostgreSQL, Axon Server).
- **Server-Side Monitoring**: `Spring Boot Actuator` endpoints (`/actuator/metrics`) will be used to capture JVM and
  application-level metrics. For advanced analysis, these can be scraped by a Prometheus and visualized in a Grafana
  dashboard.

### 2.2. Test Environment

Each test will be run against a single application instance (`crud-app` or `es-cqrs-app`) which has been freshly started
via `docker-compose`. This ensures that results are not influenced by prior test runs. The environment will be warmed up
before measurements are taken, as specified in each k6 test scenario.

### 2.3. Data Preparation

For scenarios requiring existing data (e.g., enrolling a student in a pre-existing course), a baseline dataset should be
established. This can be achieved by running a dedicated setup script before the main test execution to populate the
database with a standardized set of professors, students, and courses. This ensures reproducibility.

## 3. Key Metrics

The following metrics will be the primary indicators for comparison.

### 3.1. Client-Side Metrics (from k6)

- **Throughput**: Total requests per second (`http_reqs`). This measures the overall load the system can handle.
- **Latency / Request Duration**: The time from when a request is sent to when the response is received. We will analyze
  `avg`, `med` (median), `p(95)`, and `p(99)` to understand the typical and worst-case response times.
- **Error Rate**: The percentage of failed requests (`http_req_failed`). A key indicator of system stability under load.

### 3.2. Server-Side Metrics (from Spring Boot Actuator)

- **CPU Usage**: `system.cpu.usage` and `process.cpu.usage`.
- **Memory Footprint**: `jvm.memory.used` and `jvm.memory.max`.
- **Garbage Collection**: `jvm.gc.pause` to identify pressure on memory management.
- **Database Pool**: `hikaricp.connections.active` to monitor database connection usage.

### 3.3. ES-CQRS Specific Metric

- **Time to Consistency**: For the `impl-es-cqrs` application, this measures the delay between a command's successful
  execution (write) and the corresponding update appearing in the query model (read).

## 4. Benchmark Scenarios

The following scenarios are designed to test different aspects of the systems and highlight the architectural
trade-offs.

### Scenario 1: High-Throughput Simple Writes

- **Description**: Simulates a high volume of basic entity creation events, such as creating new course definitions for
  a new semester. This is a fundamental test of raw write performance.
- **Endpoint**: `POST /courses`
- **k6 Logic**: Each virtual user (VU) creates a unique course in a loop.
- **Hypothesis**: The `impl-crud` system may exhibit slightly lower latency due to the simplicity of a direct `INSERT`
  statement. The `impl-es-cqrs` system incurs overhead from event serialization, storage, and bus communication, which
  might result in marginally higher latency but should demonstrate stable throughput.

### Scenario 2: Complex, Validated Writes

- **Description**: Simulates students enrolling in a lecture. This is a more complex command that requires validation (
  e.g., checking for course prerequisites, student existence).
- **Endpoint**: `POST /lectures/{id}/enrollments`
- **k6 Logic**: After populating the system with a set of students and lectures, each VU will attempt to enroll a unique
  student in a specific lecture.
- **Hypothesis**: The performance difference will depend on the implementation of the validation logic.
    - **CRUD**: Validation might involve multiple `SELECT` queries to the database before the final `INSERT`,
      potentially causing database contention under load.
    - **ES-CQRS**: The validation logic is handled within the 'Lecture' aggregate in memory. If the aggregate is already
      loaded, this could be significantly faster than multiple database calls. This scenario will test the efficiency of
      the aggregate-based validation.

### Scenario 3: Simple Reads (List Retrieval)

- **Description**: Simulates a user browsing the course catalog. This is a typical read operation on a collection of
  resources.
- **Endpoint**: `GET /courses`
- **k6 Logic**: VUs repeatedly request the full list of courses.
- **Hypothesis**: Performance is expected to be very similar. Both systems will likely serve this from a simple
  `SELECT *` query on a table. The `impl-es-cqrs` read model for this is trivial and should be just as fast as the CRUD
  entity table.

### Scenario 4: Complex Reads (Aggregations)

- **Description**: Simulates a student checking their academic progress, which requires calculating their total
  accumulated credits from all graded lectures.
- **Endpoint**: `GET /stats/credits?studentId={id}`
- **k6 Logic**: VUs will request the credit statistics for a specific set of students.
- **Hypothesis**: This is where the CQRS pattern is expected to shine.
    - **CRUD**: This query likely requires complex `JOIN`s across `students`, `enrollments`, `grades`, and `courses`
      tables, which can be slow and resource-intensive on the database.
    - **ES-CQRS**: The `impl-es-cqrs` application should have a dedicated projection (read model) that is pre-calculated
      and optimized for this exact query. The request should resolve to a simple lookup on this projection table,
      resulting in significantly lower latency and higher throughput.

### Scenario 5: Measuring Eventual Consistency

- **Description**: This test is specific to `impl-es-cqrs` and measures the "read-your-writes" delay. It quantifies the
  time it takes for a change in the write model to be reflected in the read model.
- **Endpoints**:
    1. `POST /lectures/{lectureId}/grades` (Write)
    2. `GET /stats/grades?studentId={studentId}` (Read)
- **k6 Logic**:
    1. A VU assigns a new grade to a student for a lecture and records the timestamp (`t1`) upon receiving a `200 OK`.
    2. The VU immediately enters a polling loop, repeatedly calling the `GET /stats/grades` endpoint for that student.
    3. The loop continues until the new grade appears in the response. The timestamp is recorded (`t2`).
    4. The "Time to Consistency" is calculated as `t2 - t1`.
- **Hypothesis**: The time to consistency will be a non-zero value, likely in the order of milliseconds to a few seconds
  under load, depending on event handler processing time and database indexing speed. This test does not apply to the
  `impl-crud` system, which is strongly consistent.

## 5. Test Execution and Analysis

### 5.1. Execution Flow

For each scenario and for each application (`crud-app` and `es-cqrs-app`):

1. **Build Application Jars**: `./mvnw clean package -DskipTests`
2. **Start Environment**: `docker-compose up --build -d <app-name>`
3. **Run k6 Test**: `k6 run --env HOST=<host:port> <scenario-script.js>`
4. **Collect Results**: Save the k6 summary output and any relevant server-side metrics.
5. **Tear Down**: `docker-compose down -v` to ensure a clean slate for the next run.

### 5.2. Analysis

Compare the collected metrics for `impl-crud` vs. `impl-es-cqrs` for each scenario. Visualize the data using charts:

- **Latency Histograms**: To compare the distribution of response times.
- **Throughput over Time**: To see how each system sustains load.
- **CPU/Memory Usage Charts**: To compare resource consumption.

This comparative analysis will form the basis for the conclusions drawn in the thesis.
