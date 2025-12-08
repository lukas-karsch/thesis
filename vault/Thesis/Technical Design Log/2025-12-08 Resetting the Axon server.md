In my E2E tests, I repeatedly create seed data to make assertions. Through the tests, the database (containing projections) is wiped, but I need to clear the Axon event store. 
## Start Axon Server in dev mode
```java
return new AxonServerContainer( 
        DockerImageName.parse("axoniq/axonserver:latest")  
).withEnv(Map.of("axoniq.axonserver.devmode.enabled", "true"));
```
## Reset events 
```java
given()  
        .with()  
        .baseUri("http://localhost:" + axonServerContainer.getHttpPort())  
        .delete("/v1/devmode/purge-events")  
        .then()  
        .statusCode(200);
```
TODO: how can i assert that the events are actually deleted? 
## Reset tracking 
```java
eventProcessingConfiguration.eventProcessors().values().stream()  
        .filter(TrackingEventProcessor.class::isInstance)  
        .map(TrackingEventProcessor.class::cast)  
        .forEach(it -> {  
            it.shutDown();  
            it.resetTokens();  
            it.start();  
        });
```
https://github.com/vab2048/axon-exhibition/tree/main
## Why is my test failing? 
maybe instead of doing the above, i should switch to use subscribing processors in tests.
- just did that 
- it works now 
- but changes the way my app functions
- make sure to only use this in the E2E contract tests
- TURN OFF and find a different way when doing performance measurements. because
	- eventual consistency might mean that my projection fails initially, e.g. when creating the lecture projection and the course projection is not finished yet 
	- #todo think of other reasons