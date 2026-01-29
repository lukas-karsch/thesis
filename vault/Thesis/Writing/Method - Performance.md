## My notes - aggregated 
**Requirements for performance tests**
- make sure to save all data points
- make sure that test setups are identical
	- dockerize 
	- run on isolated VMs with fixed hardware (and enough to satisfy system requirements)
	- no background tasks 
- repeat the tests an appropriate number of times (usually 30)
**Implementation** 
- k6 script runs on one VM 
- other VM runs the server
	- downside here: event store (axon server) and postgres runs on the same machine. 
	- mention this! 
- VMs created using cloudinit and proxmox scripts 
	- detail those! 
	- thesis\performance-tests\vm
- python test runner orchestrates client and server; starts docker containers on the server using docker remote contexts 
- collect metrics from the server using prometheus 
- collect metrics from the client using k6 output 
- tests run on the VM using tmux, so i can disconnect 
**k6** 
- ramping arrival rate instead of ramping VUs! [[2026-01-26 Scalability and performance]]
	- "open model"
	- queuing 
**Display of results**
- currently box plot to show avg, p50, p95, p99 comparing both implementations (same amount of VUs)
  thesis\performance-tests\visualize 
- line graph to show increase for different VUs 
	- this may be "bad practice"
	- line is interpolated without real data points 
- line graph for CPU and RAM usage 
- errorbar = standard deviation 
## SLO / SLA 
- let's define SLOs 
- SLO = service level objective 
- all endpoints should have a (client) P95 latency of <= 100ms 
- all writes should be reflected in reads in < 100ms 
- failure rate of <0.1%
	- explain why requests might ever fail under load 
- then i can accurately judge which system "breaks" agreements 
## Description of load 
- Define load parameters
	- in this case: requests per second 
- designing data intensive applications, page 11 
- we answer question 1 on page 13:
	- "When you increase a load parameter and keep the system resources (CPU, memory, network bandwidth, etc.) unchanged, how is the performance of your system affected?"
- measure distribution as percentiles 
- tail latencies are important - explain why 
## What I will write 
### Theoretical Foundation of Load Testing
To accurately quantify how the systems compare in terms of scalability and performance, the load testing design follows principles from the book "Designing Data-Intensive Applications". 
- **Load Parameters**: Load is defined by requests per second (RPS) rather than just concurrent users to maintain an "open model".
	- Explain Queueing delays (p. 15f)
- **Performance Metrics**: Performance is measured by observing how system metrics are affected when a load parameter is increased while hardware resources remain constant.
	- question 1, p. 13 
- **Response Time Distribution**: Results are analyzed using percentiles (Median, P95, P99) to account for tail latencies, which are critical for understanding system behavior under stress.
### Service Level Objectives (SLO)
What is SLA and SLO? We use SLO in this thesis, as we focus on the technical aspects. 
To provide a objective basis for comparison, specific SLOs are defined to determine at what point a system "breaks" its performance promises.
Requests feel "instant" at <100ms, according to Nielsen (1993)
- **Latency SLO**: All endpoints must maintain a client-side P95 latency of $\le$ 100ms.
- **Freshness SLO**: For the Event Sourcing implementation, all writes must be reflected in the PostgreSQL projections within < 100ms.
- **Reliability SLO**: The system must maintain a failure rate of < 0.1% under load.
### Technical Test Environment
To ensure a fair comparison between the CRUD and Event Sourcing implementations, the environment is strictly controlled.
- **Infrastructure Isolation**: Tests are conducted on isolated Virtual Machines (VMs) with fixed hardware resources and no background tasks to prevent noise.
- **Provisioning**: VMs are provisioned using `cloud-init` and Proxmox scripts to ensure reproducibility.
- **Deployment Strategy**: All components are dockerized. Note that for this study, the primary database (PostgreSQL) and the Event Store (Axon Server) run on the same server VM.
- **Orchestration**: A custom Python test runner coordinates the client and server, utilizing Docker remote contexts to manage the lifecycle of containers.
### Test Execution and Tools
- **Load Generation**: k6 is used for load generation, specifically utilizing the "Ramping Arrival Rate" executor to prevent the "coordinated omission" problem inherent in closed-loop systems.
- **Execution**: Tests are run within `tmux` sessions on the client VM to allow for long-running, persistent test execution.
- **Monitoring and Data Collection**:
    - **Server-side**: Resource utilization (CPU and RAM) is collected via Prometheus.
    - **Client-side**: Latency and error rates are captured directly from k6 output.
- **Statistical Significance**: Each test scenario is repeated 30 times to ensure the results are statistically sound.
### Data Visualization and Analysis
The results are visualized to highlight the differences in scalability and resource consumption
- **Box Plots**: Used to compare the distribution of latencies (Avg, P50, P95, P99) between the two architectures at specific load levels.
- **Line Graphs**: Used to visualize how CPU and RAM usage scales as the load increases.
- **Error Bars**: Standard deviation is represented via error bars to indicate the consistency of the performance results.