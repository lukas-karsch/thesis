After attempting to design the initial API [[2025-11-09 API Design (2f6e551d)]], lets think about how to test the interface.

I want both implementations to have an identical interface, which is why I set up the shared `api` package. It will define the endpoints, return types and all DTOs. 

Inside the test-suite, I want to run identical tests on both implementations (contract and performance tests). Those will be end to end tests. 

I should be able to easily run the application together with its external dependencies, thanks to [[Testcontainers]]

Test technologies are layed out here: [[Testing in SpringBoot]]
## Test Design 
### End to end tests 
Those will be contract tests - no performance. Test that the interfaces are implemented correctly and identically by both applications. 

## Problem with Axon

I tried the following approach:

- test-suite package contains two application runners that start the impl-crud and impl-es-cqrs implementations
- problem was: the impl-crud started together with the axon server container (and then couldnt even connect to it?)
- reason unclear
    - **maybe because axon-test is on the classpath**
      something like this is probably most likely, because if the bean was created and the app tried to connect to it,
      it would 1) succeed 2) show the running docker image on docker desktop
    - maybe because the bean was created (despite the `@Profile` annotation) and the spring app connected to it due to
      `@ServiceConnection`

### Solution

- main() method MUST be static in ApplicationRunner
- updated axon version to 4.11.3 (latest) https://github.com/AxonFramework/axon-bom
