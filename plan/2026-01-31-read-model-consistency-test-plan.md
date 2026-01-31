# Read-Model Consistency Performance Test Plan

**Date:** January 31, 2026

## 1. Objective

To measure the latency between the successful confirmation of a "fire-and-forget" command and the visibility of that
command's outcome in the read model. This test specifically measures the time taken for event persistence, propagation,
and read-model projection, excluding any client-side or server-side synchronous waiting.

## 2. Key Endpoint Selection

To achieve this, we need a command endpoint that returns immediately upon successful validation and a separate query
endpoint to poll for the result.

- **Write (Command):** `POST /lectures/create`
    - **Analysis:** This endpoint takes a `CreateLectureRequest`, sends a `CreateLectureCommand`, and, upon successful
      processing by the command model, returns a `201 CREATED` response with the new `lectureId`. It does **not** use a
      subscription query and returns quickly, making it ideal for this test.
- **Read (Polling):** `GET /lectures/{lectureId}`
    - **Analysis:** This endpoint is used to fetch the details of a specific lecture. We will poll this endpoint with
      the `lectureId` received from the write command. Initially, it will return a `404 Not Found` until the read model
      is updated, at which point it will return a `200 OK`.

## 3. Measurement Strategy

The time to consistency will be measured using the following sequence within a k6 virtual user (VU):

1. **Execute Write:** The VU sends a `POST /lectures/create` request.
2. **Receive Confirmation:** The VU receives the `201 Created` response and the new `lectureId`.
3. **Start Timer:** Immediately after the response is received, the VU records a start time.
4. **Poll Read Model:** The VU enters a polling loop, repeatedly sending `GET /lectures/{lectureId}` requests. A short
   delay (e.g., 100ms) should be placed between polls to avoid overwhelming the server.
5. **Check for Visibility:** Inside the loop, the VU checks the response status.
    - If the status is `200 OK`, the change is visible.
6. **Stop Timer:** The VU records the end time, calculates the total duration (`endTime - startTime`), and records this
   value in a custom k6 `Trend` metric named `read_model_consistency_time`.
7. A timeout/max-retry limit must be used for the polling loop to handle cases where the read model does not become
   consistent in a reasonable time.

## 4. Test Implementation Plan (k6)

A new k6 script will be created at
`performance-tests/k6/writes/measure-read-model-consistency/measure-read-model-consistency.js`.

### 4.1. `setup()` Function

- Create prerequisite data that is not part of the measurement loop:
    - A single `professor` user.
    - A set of `courses`.
- Return the `professorId` and `courseIds` to the main test function.

### 4.2. `default()` (VU) Function

- Each VU picks a `courseId` from the setup data.
- **Execute Write:** Send a `POST /lectures/create` request using the professor's credentials and a course ID.
- Verify the response is `201 Created` and extract the new `lectureId`.
- **Start Timer:** `const startTime = new Date();`
- **Polling Loop:**
  ```javascript
  let successfulPoll = false;
  for (let i = 0; i < 20; i++) { // Max 20 retries
      const res = http.get(`${TARGET_HOST}/lectures/${lectureId}`);
      if (res.status === 200) {
          const endTime = new Date();
          const consistencyTime = endTime - startTime;
          readModelConsistencyTrend.add(consistencyTime); // Custom trend metric
          successfulPoll = true;
          break;
      }
      sleep(0.1); // Wait 100ms between polls
  }
  if (!successfulPoll) {
      // Record a failure if the loop timed out
  }
  ```
- A custom k6 `Trend` metric must be defined:
  `const readModelConsistencyTrend = new Trend('read_model_consistency_time');`

### 4.3. `options` Configuration

- Use the `ramping-arrival-rate` executor to control the rate of new lectures being created.
- **Thresholds** should be set on the custom trend and failure rate:
  ```javascript
  thresholds: {
      'http_req_failed': ['rate<0.01'],
      'read_model_consistency_time': ['p(95)<500'], // 95% of reads should be consistent in under 500ms
  },
  ```

RPS is a little different to calculate here. Probably just have to take the total number of requests sent by k6 and
divide by test duration.

## 5. Next Steps

1. Create the directory: `performance-tests/k6/writes/measure-read-model-consistency/`.
2. Implement the `measure-read-model-consistency.js` script as detailed above.
3. Update `perf_runner.py` to include an entry for executing this new test.
4. Execute a series of test runs with varying `target` RPS values.
5. Analyze the `read_model_consistency_time` trend (p95, median) against the system load (RPS) to understand the
   performance characteristics of the read-model projection.
