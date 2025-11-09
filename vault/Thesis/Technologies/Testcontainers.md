https://docs.spring.io/spring-boot/reference/testing/testcontainers.html  
  
The applications relies on external services (postgres and axon). To make the workflow simple, I set up testcontainer support in both implementations. Those testcontainers are started together with the spring application by using SpringApplication.from(...).with(MyDependency.class).   
  
The testcontainers are persisted via program restarts by using the `@RestartScope` annotation on the beans.