## Testing write latency changes 
![[Pasted image 20260103201102.png]]
Can see how latency increases during ramp-up, then decreases and is finally lower than at the start (cold start artefact?)
-> have to look at more endpoints, more VUs, longer duration. 
Code used for the above plot:
```PromQL
histogram_quantile(
  0.5,
  sum by (le, uri, method) (
    rate(http_server_requests_seconds_bucket[10s])
  )
)
```
## Repeated runs on "GET /lectures"
Test "get lectures for student" endpoint.
**Test description**:
- Create 50 courses
- Create a lecture for each course 
- Create 100 students 
- Every student is enrolled to a lecture 
- Repeatedly: call `GET /lectures`, an endpoint which returns lectures a student is enrolled in
**Options:**
```json
stages: [  
    {duration: '10s', target: 20}, // Ramp-up to 20 virtual users over 30s  
    {duration: '1m', target: 20},  // Stay at 20 virtual users for 1 minute  
    {duration: '10s', target: 0},   // Ramp-down to 0 users  
]
```
### 20 VUs 
![[plot_get_lectures_latency_comparison.png]]
Questions / To do:
- measure with more VUs 
- relative comparison (1:x)
### 50 VUs
![[plot_get_lectures_latency_comparison_50vu.png]]
### Latency vs. load 
Plot virtual users vs. latency 
![[plot_get_lectures_latency_vs_virtual_users.png]]

