#performance 
To do the performance tests, I will dockerize the application. 
## Steps 
1. Make sure both applications can build with maven 
2. Dockerize both applications 
	1. Dockerfile 
	2. docker-compose.yml (created with help from gemini)
3. Install k6 
4. Write first test script 
5. Write launch configurations for IntelliJ
## Problems 
### Maven / Build 
My apps are not building.
What I had to do to fix it:
- **Lombok**: add build plugin: https://projectlombok.org/setup/maven
- **Dependencies**: Had trouble with the "test-jar" dependency on `test-suite`. Had to add a `<goal>` to the `test-suite` pom.xml
## Dockerizing
After fixing the `mvn clean package` errors, i tried to run `docker-compose up --build -d crud-app`. This seemed to work, but the spring container immediately exited with this message: no main manifest attribute, in application.jar

Apparently, a spring maven build plugin was missing:
```xml
<plugin>  
    <groupId>org.springframework.boot</groupId>  
    <artifactId>spring-boot-maven-plugin</artifactId>  
</plugin>
```
Yes, that was it! 
CRUD application is running ✅

Now, let's try to get the ES-CQRS app running - might be a bit more trouble due to the additional dependency on Axon. I tried setting the application.properties as well as i could - probably something is missing related to axon configuration. 
Added the following line to application.properties:
`axon.axonserver.servers=${AXON_HOST:localhost}`
Results in the following error:
```
: Requesting connection details from axon-server:8124

2025-12-19T15:27:28.565Z WARN 1 --- [ main] i.a.a.c.impl.AxonServerManagedChannel : Connecting to AxonServer node [axon-server:8124] failed.

io.grpc.StatusRuntimeException: NOT_FOUND: [AXONIQ-1302] default: not found in any replication group
```
Now, explicitly set the hostname for the container: 
```yml 
axon-server:  
  image: axoniq/axonserver:latest  
  hostname: axon-server  
  ports:  
    - "8024:8024"  
    - "8124:8124"  
  environment: # TODO clean up  
    - AXONSERVER_DEVMODE_ENABLED=true # these properties might need prefix "AXONIQ"
    - AXONSERVER_HOSTNAME=axon-server  
```
And change this in `application.properties`
```
axon.axonserver.servers=${AXON_HOST}:8124
```
-> still not working. 
### Problem: no default context 
Axon server needs a context to exist. When i go to the dashboard, I can create one. But that should happen automatically 
-> set standalone mode: AXONIQ_AXONSERVER_STANDALONE=true 
https://docs.axoniq.io/axon-server-reference/development/axon-server/installation/local-installation/#_non_clustered
