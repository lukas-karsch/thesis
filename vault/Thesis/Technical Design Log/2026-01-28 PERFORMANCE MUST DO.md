#performance 
**Server side:** 
- first, check and define heap memory of the JVM 
- count database connections, check database pool size 
- measure thread pool saturation (and check if tunable)
these things are rlly important, if i don't tune, at least mention when interpreting results 

some perf optimization tips in here https://dev.to/mohit_bajaj_a3e3241d02fda/how-i-optimized-a-spring-boot-application-to-handle-1m-requestssecond-2i78
## Measure 
Current active connections: `hikaricp_connections_active` (Gauge)
`hikaricp_connections_max` = 10

`tomcat_threads_current_threads`
