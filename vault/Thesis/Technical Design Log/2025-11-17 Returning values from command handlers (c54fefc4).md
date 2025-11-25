**I thought:**
> CQRS = command handlers never return anything 

Apparently, it is "allowed" / supported to return values, if they come directly from the aggregate and are _not_ fetched from projections.
## Example 
Enrolling a student -> the aggregate will decide whether the student can be enrolled -> can immediately return a value! 
## So...?
I think from some "command handler" endpoints it will make sense to return a simple result. 
> Return aggregate decisions, not model data! 
## Links
https://chatgpt.com/c/691b0436-6bb4-832c-ad59-188445d2d2e1
[[[CQRS (Command Query Responsibility Segregation)]]]