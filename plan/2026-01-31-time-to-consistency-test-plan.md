# Time to Consistency Performance Test Plan

**Date:** January 31, 2026

## 1. Objective

To measure the "time to consistency" for the student enrollment action in the `impl-es-cqrs` application under various
load conditions (Requests Per Second). This will help quantify the performance of the event-sourced system and identify
at what load level the consistency time begins to degrade.

## 2. Key Endpoint Analysis

- **Write Operation:** `POST /lectures/{lectureId}/enroll`
- **Analysis:** An investigation of the `LecturesController.enrollToLecture` method revealed that it uses an Axon
  `subscriptionQuery`. This means the HTTP request handler internally waits for the read model to be updated before
  sending a response to the client. It has a hardcoded server-side timeout of 10 seconds.

## 3. Measurement Strategy

- Because the endpoint waits for consistency, complex client-side polling is not required.
- The **"time to consistency" can be directly measured by the `http_req_duration`** of the
  `POST /lectures/{lectureId}/enroll` request, as captured by k6.
- Requests that take longer than the 10-second server-side timeout will fail. The `http_req_failed` rate in k6 will
  therefore measure the percentage of times the system failed to achieve consistency within the required window.

## 4. Test Implementation Plan (k6)

A new k6 script will be created, for example at
`performance-tests/k6/writes/measure-enrollment-consistency/measure-enrollment-consistency.js`.

### 4.1. `setup()` Function

The setup phase must create all prerequisite data without performing the action being measured.

- Create a single `professor` user.
- Create a sufficient number of `courses` and a corresponding `lecture` for each course.
- Advance the lifecycle of all created lectures to `OPEN_FOR_ENROLLMENT`.
- Create a pool of unique `student` users, ideally at least one per expected VU, to ensure each enrollment is a unique
  event.
- Return the `lectureIds` and `studentIds` to the main test function.

### 4.2. `default()` (VU) Function

This function represents the logic for each virtual user.

- Each VU will select a unique student and a lecture to enroll in. This can be achieved using the `__VU` and `__ITER` k6
  variables to map VUs to students.
- Execute a single `http.post()` request to `/lectures/{lectureId}/enroll`, including the student's ID in the
  `customAuth` header.
- Check that the response code is `201 Created` to confirm a successful enrollment.
- k6 will automatically measure the duration of this request, which serves as our primary metric.

### 4.3. `options` Configuration

The test should be configured to run with a controlled arrival rate to test different load levels.

- Use the `ramping-arrival-rate` executor to precisely control the load.
- **Example `stages` for a single run (e.g., 20 RPS):**
  ```javascript
  stages: [
      { target: 20, duration: "20s" }, // Ramp up to 20 RPS
      { target: 20, duration: "80s" }, // Maintain 20 RPS for 80s
      { target: 0, duration: "20s" }  // Ramp down
  ]
  ```
- **Thresholds** should be set to define success criteria:
  ```javascript
  thresholds: {
      'http_req_failed': ['rate<0.01'],        // Error rate (including timeouts) must be less than 1%
      'http_req_duration': ['p(95)<1000'],     // 95% of requests must achieve consistency in under 1 second
  },
  ```

## 5. Execution and Orchestration

- The existing `perf_runner.py` script should be updated to include a new test definition that runs the new
  `measure-enrollment-consistency.js` script.
- The test should be executed multiple times. For each execution, the `target` RPS in the k6 script should be adjusted (
  e.g., 10, 25, 50, 100, 150 RPS) to observe how `http_req_duration` (consistency time) changes with system load.

## 6. Metrics for Analysis

- **Primary Metric:** `http_req_duration` (from k6): Provides avg, median, p(95), and p(99) for time to consistency.
- **Secondary Metric:** `http_req_failed` (from k6): Indicates the rate at which the system fails to achieve consistency
  within the 10s timeout.
- **System Metrics:** CPU and RAM usage of the application, database, and message broker (collected via Prometheus, as
  is current practice) to correlate performance with resource consumption.

## 7. Next Steps

1. Create the new directory: `performance-tests/k6/writes/measure-enrollment-consistency/`.
2. Implement the `measure-enrollment-consistency.js` script as described above.
3. Add a new entry in `perf_runner.py` to allow execution of this test.
4. Execute a series of test runs with varying load levels.
5. Analyze and visualize the resulting k6 summary statistics and Prometheus graphs to report on time-to-consistency vs.
   system load.
