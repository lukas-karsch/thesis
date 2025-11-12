- Initial project setup   
- Maven multi module project:  
  - api (will contain the API definition with its public interface and DTOs)  
  - impl-crud   
  - impl-es-cqrs  
  - test-suite (will contain a test-suite to test the public API and performance tests)  
  
Both the `impl-crud` and `impl-es-cqrs` project are set up and can run using testcontainers. (`CrudApplicationRunner.java` and `EsCqrsApplicationRunner.java`)

Set up [[Testcontainers]]
Using [[Spring Devtools]]
