I will run performance tests using [[k6]] from the CLI. The question is
> Should i measure my metrics on the client (k6 results) or on the server? 

On the client is easy, that would just be the k6 results. But network might not be a negligible factor! It would most likely be cleaner and more accurate to measure response times on the server (time between request in and response out).

Especially if I plan to move the client and server to VMs (maybe the client on a different machine), the network will be a huge factor! 
## Actuator 
Springboot actuator already seems to do what i want. 
```gradle
implementation "org.springframework.boot:spring-boot-starter-actuator"
implementation "io.micrometer:micrometer-registry-prometheus" // optional
```
Get metrics here: 
```http
GET /actuator/metrics/http.server.requests
```
## Workflow 
https://chatgpt.com/c/695274e3-8b04-832d-ad73-5a61c7954654
Using the prometheus dependency from above 
### Spring config:
```yaml 
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
```
-> histogram needed to get accurate percentiles 
```http
GET /actuator/prometheus
```
## TODO 
- save k6 results 
- add prometheus dependency 
	- check if metrics work while running 
- write reusable, configurable script 
	- choose which k6 script is run 
	- save results 
	- start prometheus 
	- save prometheus (server-side) metrics 
	- clean data
-> all done! 
