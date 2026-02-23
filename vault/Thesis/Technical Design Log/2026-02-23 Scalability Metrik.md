Wie berechne ich meine Scalability Metrik? 
**Formel für productivity metrik:** 
$F(k)=\lambda(k) \cdot f(k) / C(k)$

| **Variable** | **Metric**            | **Definition / Formula**                                                           |
| ------------ | --------------------- | ---------------------------------------------------------------------------------- |
| $k$          | **Scale Factor**      | RPS                                                                                |
| $\lambda(k)$ | **Throughput**        | Responses per second $\rightarrow$ `iterations` - `dropped_iterations`             |
| $f(k)$       | **Value of response** | Derived from $latency\_p95$                                                        |
| $C(k)$       | **Running cost**      | CPU Usage + Storage Size $S$+ Threadpool Saturation $T$ + Database connections $D$ |
**Cost function**
$$C(k) = w_1 * \text{CPU} + w_2 * \text{S} + w_3 \left( \frac{T}{200} \right) ^2 + w_4 \left( \frac{D}{10} \right)^2$$
Can be weighted. 
Let's try setting all $w_i$ to 1, except for $w_4$, assuming storage isn't a big bottleneck 
**Formel für Scalability:** 
$\psi(k_1, k_2) = \frac{F(k_2)}{F(k_1)}$
## Scalability Results 
### L1 - Create Lecture Simple 
k1: 500 RPS 
k2: 1000RPS 

`storage_ratio` for CRUD -> based on assumption of linear growth in ES-CQRS. Data is not accurate due to projection lag. 

**CRUD**
Productivity at k1 (F1): 494.7254
Productivity at k2 (F2): 710.4652
Scalability (ψ): 1.4361
**ES-CQRS**
Productivity at k1 (F1): 15.09
Productivity at k2 (F2): 0.1956
Scalability (ψ): 0.013

---
k1: 200 RPS
k2: 500 RPS

**CRUD**
Productivity at k1 (F1): 94.7504
Productivity at k2 (F2): 511.2016
Scalability (ψ): 5.3952 # ridiculously high because latency_p95 goes down between 200 and 500RPS
**ES-CQRS**
Productivity at k1 (F1): 24.1983
Productivity at k2 (F2): 15.0921
Scalability (ψ): 0.6237
### L2 - Create Lecture Prerequisites 
k1: 500 RPS 
k2: 1000RPS 

**CRUD**
Productivity at k1 (F1): 351.5559
Productivity at k2 (F2): 390.7266
Scalability (ψ): 1.1114
**ES-CQRS**
Productivity at k1 (F1): 8.0627
Productivity at k2 (F2): 0.1858
Scalability (ψ): 0.0230

---

k1: 200 RPS
k2: 500 RPS

**CRUD**
Productivity at k1 (F1): 140.2068
Productivity at k2 (F2): 351.5559
Scalability (ψ): 2.5074
**ES-CQRS**
Productivity at k1 (F1): 22.9884
Productivity at k2 (F2): 8.0627
Scalability (ψ): 0.3507
### L3 - Enrollment 
k1: 50 RPS 
k2: 100 RPS 

**CRUD**
Productivity at k1 (F1): 24.8065
Productivity at k2 (F2): 68.5442
Scalability (ψ): 2.7632
**ES-CQRS**
Productivity at k1 (F1): 1.3843
Productivity at k2 (F2): 0.2699
Scalability (ψ): 0.1950
## L4: Read lectures 
k1: 2000 RPS 
k2: 3000 RPS 

**CRUD**
Productivity at k1 (F1): 3587.5726
Productivity at k2 (F2): 2121.4689
Scalability (ψ): 0.5913
**ES-CQRS**
Productivity at k1 (F1): 428.0378
Productivity at k2 (F2): 122.4477
Scalability (ψ): 0.2861
#todo für mehr RPS
### L5: Get  Credits 
k1: 1000
k2: 2000

**CRUD**
Productivity at k1 (F1): 1114.1652
Productivity at k2 (F2): 477.3087
Scalability (ψ): 0.4284
**ES-CQRS**
Productivity at k1 (F1): 618.6817
Productivity at k2 (F2): 697.0280
Scalability (ψ): 1.1266

---

k1: 2000
k2: 3000

**CRUD**
Productivity at k1 (F1): 477.3087
Productivity at k2 (F2): 0.6232
Scalability (ψ): 0.0013
**ES-CQRS**
Productivity at k1 (F1): 697.0280
Productivity at k2 (F2): 214.2646
Scalability (ψ): 0.3074
### L6: Time to consistency 
Cost formula gets a slight adjustment: 
$R$ = visible read rate 
$w_5=3$
$latency\_p95$ is: POST + GET added 
$$C(k) = w_1 * \text{CPU} + w_2 * \text{S} + w_3 \left( \frac{T}{200} \right) ^2 + w_4 \left( \frac{D}{10} \right)^2 + w_5 * (1-R) ^4 $$
k1 = 300
k2 = 400

**CRUD**
Productivity at k1 (F1): 114.2988
Productivity at k2 (F2): 134.3992
Scalability (ψ): 1.1759
**ES-CQRS**
Productivity at k1 (F1): 40.1250
Productivity at k2 (F2): 14.0060
Scalability (ψ): 0.3491
### L7: Reconstruction 
k1: 500 RPS 
k2: 1000 RPS 

**CRUD**
Productivity at k1 (F1): 352.8362
Productivity at k2 (F2): 690.4967
Scalability (ψ): 1.9570
**ES-CQRS**
Productivity at k1 (F1): 159.8456
Productivity at k2 (F2): 186.3937
Scalability (ψ): 1.1661

---

k1: 1000 RPS 
k2: 2000 RPS

**CRUD**
Productivity at k1 (F1): 2154.0919
Productivity at k2 (F2): 2861.7574
Scalability (ψ): 1.3285
**ES-CQRS**
Productivity at k1 (F1): 193.5176
Productivity at k2 (F2): 0.2863
Scalability (ψ): 0.0015
