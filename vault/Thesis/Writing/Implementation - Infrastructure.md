## Notes 
- Testcontainers 
- Dockerfiles, docker-compose.yml 
- VM provisioning, proxmox script and cloudinit 
- (optional: connect via ssh; long-running tests in tmux)
### Relevant files
- /performance-tests/vm
- /docker-compose.yml
- /impl-crud/Dockerfile
- /impl-crud/src/main/resources/application.properties # specifically configuration for postgres, env variables passed through docker compose 
- /impl-es-cqrs/Dockerfile
- /impl-es-cqrs/src/main/resources/application.properties # specifically the configuration regarding Axon server 
## Draft
```xml
<Instructions>
	<language>english</language>
	<style>formal, simple, precise</style>
	<additional>Read all relevant files from the top of this file. Use the notes at the top of this file.</additional>
</Instructions>
```

The project's infrastructure is designed for consistency and reproducibility across development and testing environments. It is composed of a containerized environment for running the applications and their dependencies, an automated virtual machine (VM) provisioning setup for performance testing, as well as an integration testing strategy using Testcontainers, described in section (TODO).
### Containerized Services
The core of the infrastructure is defined in a `docker-compose.yml` file at the root of the project, which orchestrates the deployment of the two primary applications and their external dependencies: a PostgreSQL database, used by both applications, and an Axon Server instance, used by the ES-CQRS application.

A `postgres:18-alpine` container provides the relational database used by both applications. The database schema, user, and credentials are configured through environment variables. A volume is used to persist data across container restarts.

An `axoniq/axonserver` container provides the necessary infrastructure for the Event Sourcing and CQRS implementation, handling event storage and message routing. It is configured to run in development mode.

The CRUD and ES-CQRS applications are containerized using `Dockerfile`s. Both use `amazoncorretto:25` as the base image, and the compiled Java application (`.jar` file) is copied into the container and executed.

Configuration details, such as database connection strings and server hostnames, are externalized from the `application.properties` files. They are injected into the application containers at runtime as environment variables via the `docker-compose.yml` file, allowing for flexible configuration without modifying the application code.
### Local Development and Integration Testing
For local development and integration testing, the project uses the Testcontainers library. This approach allows developers to programmatically define and manage the lifecycle of throwaway Docker containers for dependencies like PostgreSQL and Axon Server directly from the test code. (TODO duplicate?)

By integrating with Spring Boot's Testcontainers support, running the application or its tests automatically starts the required containers. This eliminates the need to manually install and manage these services on their local machines, ensuring a consistent and isolated testing environment. The configuration for this is found in the test resources, where a special JDBC URL prefix signals Spring Boot to manage the database container.
### VM Provisioning for Performance Testing
To ensure a stable and isolated environment for performance benchmarks, a dedicated VM setup is used. The process of creating and provisioning these VMs on a Proxmox host is fully automated.

A shell script, `create-vm.sh`, orchestrates the creation of a VM template from an Ubuntu 24.04 cloud image. Cloud images are pre-configured, lightweight variants of operating systems. This script works in conjunction with a `cloud-init.yml` configuration file that handles the provisioning of the VM upon its first boot. 

During the provisioning process, a number of steps are executed. First, it is made sure that the system is up to date by installing any available software updates. Next, a `thesis` user is created for which the environment is configured. Afterwards, the script installs all necessary software, including Docker, git, Conda, Python, k6, Maven, and Java 25. Once all necessary software is installed, the project's git repository is cloned and a Maven build is triggered. Finally, the Docker images are built. After these steps are completed, the provisioned VM is ready to run the applications and load tests.
Instead of starting the VM directly, the script shuts the VM down and converts it into a Proxmox template, which can be re-created efficiently. 
