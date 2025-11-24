To toggle between different functions and features at runtime, I implemented actuator endpoints at /actuator/features. 
Features can easily be added in the `Features` enum.
## Use Case 
Right now I am using it in `impl-crud/StatsController` to decide which `getAccumulatedCredits` function to use. 