## Load testing 
"Designing data intensive applications" book.
### Learnings 
- Load test should NOT wait for request to complete, then sleep 
- https://gemini.google.com/app/7b0c3b1d010fea99
- "open model": per second, X new VUs start making requests 
- queueing problem 
- Ramping arrival rate https://grafana.com/docs/k6/latest/using-k6/scenarios/executors/ramping-arrival-rate/
## SLA and SLO 
Service level agreement / service level objective 

should i define those for my service (e.g. no endpoint should have a P99 latency of > 100ms)?
then i can accurately judge which system "breaks" SLAs under which load 
## How to measure 
- Book says to measure on the client so that the "queue" on the server is included in measurements
	- client = network 
	- wanted to ignore network, so measure on the server 
	- but have to make sure that the request gets recorded IMMEDIATELY, not only when it starts getting processed! 
![[Pasted image 20260126165850.png]]
![[Pasted image 20260126165908.png]]
This problem can be observed in my measurements! 
-> CPU usage at 100%; requests are stalled before reaching the filter which measures request time 
-> looks like i have to use client request durations! network on localhost can probably be ignored 
-> server response times are still relevant; because they show which parts of the code are slow processing 