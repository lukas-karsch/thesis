>Monitor your applications, systems, and services with the leading open source monitoring solution. Instrument, collect, store, and query your metrics for alerting, dashboarding, and other use cases.
https://prometheus.io/
## Query 
Prometheus provides a functional query language called PromQL (Prometheus Query Language) that lets the user select and aggregate time series data in real time.
https://prometheus.io/docs/prometheus/latest/querying/basics/
## Run container (standalone)
Start prometheus docker container: 
```bash
cd performance-tests

docker run -d --name prometheus -p 9090:9090 -v \ .\prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
```
