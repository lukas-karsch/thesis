# Performance Testing Plan: CRUD vs. ES-CQRS

This document outlines the strategy for performance testing and comparing the `impl-crud` and `impl-es-cqrs` Spring Boot
applications. We will use a black-box approach, containerizing the entire environment with Docker and using k6 for load
testing.

## Phase 1: Environment Setup with Docker

To ensure isolated and reproducible test runs, we will containerize each application and its dependencies (Postgres,
Axon Server).

### Step 1.1: Create a Dockerfile for the Applications

Create the following `Dockerfile` in the root directory of **both** the `impl-crud` and `impl-es-cqrs` modules. This
file will be used to build a Docker image for each application.

**`Dockerfile`**

```dockerfile
# Use a slim JDK image for a smaller footprint
FROM openjdk:17-jdk-slim

# Set a working directory
WORKDIR /app

# Argument to accept the JAR file name
ARG JAR_FILE=target/*.jar

# Copy the packaged jar file into the container
COPY ${JAR_FILE} application.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "application.jar"]
```

### Step 1.2: Configure Spring Applications for Docker

Modify the `application.properties` file in both `impl-crud/src/main/resources` and `impl-es-cqrs/src/main/resources` to
use environment variables. This allows Docker Compose to configure the database and Axon connections.

**`application.properties` (example changes)**

```properties
# ... other properties
# Use environment variables with default values for local running
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:postgres}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update
# For the ES-CQRS application
axoniq.axonserver.servers=${AXON_HOST:localhost}
```

### Step 1.3: Create a `docker-compose.yml`

Create a `docker-compose.yml` file in the project's root directory. This file defines all the services and their
configurations.

**`docker-compose.yml`**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    environment:
      - POSTGRES_DB=thesis_db
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U user -d thesis_db" ]
      interval: 10s
      timeout: 5s
      retries: 5

  axon-server:
    image: axoniq/axonserver:latest-jdk-17-dev
    ports:
      - "8024:8024"
      - "8124:8124"
    environment:
      - AXONIQ_AXONSERVER_DEVMODE_ENABLED=true

  crud-app:
    build:
      context: ./impl-crud
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=thesis_db
      - DB_USER=user
      - DB_PASSWORD=password
    depends_on:
      postgres:
        condition: service_healthy

  es-cqrs-app:
    build:
      context: ./impl-es-cqrs
      dockerfile: Dockerfile
    ports:
      - "8081:8080" # Run on a different host port to avoid conflicts
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=thesis_db
      - DB_USER=user
      - DB_PASSWORD=password
      - AXON_HOST=axon-server
    depends_on:
      postgres:
        condition: service_healthy
      axon-server:
        condition: service_started

volumes:
  postgres_data:
```

## Phase 2: Implement Load Test with k6

[k6](https://k6.io/) is a modern, open-source load testing tool. We'll write a simple test script in JavaScript.

### Step 2.1: Install k6

Follow the [official k6 installation guide](https://k6.io/docs/getting-started/installation/) for your operating system.

### Step 2.2: Create a Test Script

Create a file named `test-script.js` in the root of your project. This script will simulate users creating courses by
sending POST requests to the `/courses` endpoint.

**`test-script.js`**

```javascript
import http from 'k6/http';
import {check, sleep} from 'k6';

// The host to target, configurable via an environment variable
const TARGET_HOST = __ENV.HOST || 'http://localhost:8080';

// Test options
export const options = {
    stages: [
        {duration: '30s', target: 20}, // Ramp-up to 20 virtual users over 30s
        {duration: '1m', target: 20},  // Stay at 20 virtual users for 1 minute
        {duration: '10s', target: 0},   // Ramp-down to 0 users
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'], // 95% of requests must complete below 500ms
        'http_req_failed': ['rate<0.01'],    // Error rate must be less than 1%
    },
};

// The main test function (Virtual User code)
export default function () {
    const url = `${TARGET_HOST}/courses`;

    const payload = JSON.stringify({
        "name": `Performance Test Course ${__VU}-${__ITER}`,
        "description": "A course created during performance testing.",
        "assessments": [{"weight": 1, "assessmentType": "PAPER"}],
        "credits": 5,
        "prerequisiteCourseIds": []
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'customAuth': 'professor_1' // Assuming this is a required header
        },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'is status 200 or 201': (r) => r.status === 200 || r.status === 201,
    });

    sleep(1); // Wait for 1 second between requests per VU
}
```

## Phase 3: Execution and Analysis

Follow this workflow to test each application separately.

### Step 3.1: Package the Applications

First, build the JAR files for both applications, skipping tests to speed up the process.

```bash
./mvnw clean package -DskipTests
```

### Step 3.2: Test the `impl-crud` Application

1. **Start the environment:** This command builds and starts the `crud-app` and its `postgres` dependency.
   ```bash
   docker-compose up --build -d crud-app
   ```

2. **Run the k6 test:** Target the `crud-app` running on port `8080`.
   ```bash
   k6 run --env HOST=http://localhost:8080 test-script.js
   ```

3. **Collect results:** Save the output from the k6 command.

4. **Tear down the environment:**
   ```bash
   docker-compose down
   ```

### Step 3.3: Test the `impl-es-cqrs` Application

1. **Start the environment:** This command starts the `es-cqrs-app` and its dependencies. Note it runs on host port
   `8081`.
   ```bash
   docker-compose up --build -d es-cqrs-app
   ```

2. **Run the k6 test:** Target the `es-cqrs-app` running on port `8081`.
   ```bash
   k6 run --env HOST=http://localhost:8081 test-script.js
   ```
3. **Collect results:** Save the output from the k6 command.

4. **Tear down the environment:**
   ```bash
   docker-compose down -v # Use -v to remove the postgres volume if you want a clean slate
   ```

### Step 3.4: Analyze the Results

Compare the summary statistics from k6 for both applications:

- **`http_req_duration`**: Look at `avg`, `min`, `med`, `p(90)`, `p(95)`, `max`. The p(95) and p(90) values are often
  more telling than the average.
- **`http_reqs`**: This is your throughput (requests per second).
- **`http_req_failed`**: The percentage of failed requests.

## Phase 4: Best Practices and Advanced Metrics

- **JVM Warm-up**: The first few requests to a Java application are always slow. The ramp-up stage in the k6 script (
  `{ duration: '30s', target: 20 }`) acts as a warm-up period. For more serious testing, consider a dedicated warm-up
  run that you discard before the measurement run.
- **Server-Side Metrics**: k6 only shows you what the client sees. To understand what's happening inside your
  application, use **Spring Boot Actuator**.
    1. Add the Actuator dependency to your `pom.xml`:
       ```xml
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-actuator</artifactId>
       </dependency>
       ```
    2. Expose the metrics endpoint in `application.properties`:
       ```properties
       management.endpoints.web.exposure.include=health,info,metrics,prometheus
       ```
    3. While a test is running, you can inspect metrics by visiting `http://localhost:8080/actuator/metrics` or
       `http://localhost:8080/actuator/metrics/[metric.name]` (e.g., `jvm.memory.used`). This is invaluable for spotting
       memory leaks or high CPU usage.
- **Automated Dashboards (Advanced)**: For the most professional setup, add Prometheus and Grafana to your
  `docker-compose.yml` to automatically scrape metrics from the Actuator's Prometheus endpoint and visualize them in
  real-time dashboards. This gives you deep insight into performance over the entire test duration.
