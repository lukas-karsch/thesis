After implementing the audit log ([[2025-11-14 Audit Log (c5367a6b)]], I am almost ready to start implementing the applications. 
First, I want to create the tests - let's do a little bit of TDD! I am going to add some tests according to my requirements, implement the code accordingly, and keep going. 

This means I need to figure out how to run actual E2E tests on a running application - let's do that now! 
## E2E Tests 
#e2e
**What I tried:**
### test-suite starts the apps 
I tried to import the `impl-crud` and `impl-es-cqrs` projects into the `test-suite` project. Then I wanted to do `@SpringBootTest` and run both applications. But the dependencies of both implementations were dragged into the test-suite, leading to classpath leaking. 

This means that the `impl-crud` project tried to use axon, which it should not. Fighting SpringBoot's auto configuration is pointless, so I tried something else. 
### Import the .jar files only 
ChatGPT suggested to import the jars of both implementations and start those via the process API. This may have worked but there were some downsides:
- have to start the test via maven -> lose IntelliJ insights when running the tests 
- Apps would have to have managed their own testcontainers via beans 
	- -> Additional configuration using @Profile
- Logs not shown 
### > What worked: reversing the dependency 
Now, both implementations import the `test-suite` package via test-jar. `test-suite` includes abstract base classes that can simply be extended by both implementations. Both implementations can manage their own testcontainer setup via test code. 

ChatGPT said this was a bad approach but so far I am happy with it. It's clean, both app test directories manage their own external infrastructure and I get no classpath leakage. 
## CoursesController
Today, I implemented the `impl-crud` CoursesController. Both endpoints that are currently contained in the interface (`GET` and `POST`) are implemented and access the repository. 