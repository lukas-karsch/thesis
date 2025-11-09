After attempting to design the initial API [[2025-11-09 API Design (2f6e551d)]], lets think about how to test the interface.

I want both implementations to have an identical interface, which is why I set up the shared `api` package. It will define the endpoints, return types and all DTOs. 

Inside the test-suite, I want to run identical tests on both implementations (contract and performance tests). Those will be end to end tests. 

I should be able to easily run the application together with its external dependencies, thanks to [[Testcontainers]]

Test technologies are layed out here: [[Testing in SpringBoot]]
## Test Design 
### End to end tests 
Those will be contract tests - no performance. Test that the interfaces are implemented correctly and identically by both applications. 