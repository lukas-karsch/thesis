Cross-aggregate validation in DDD / event sourcing is really hard due to the eventual consistency.
## Lookup tables 
It is possible to create lookup tables which belong solely to the command side. They can be made immediately consistent and are therefore "designed" to be used in command validation. 
Article here: https://www.axoniq.io/blog/2020set-based-consistency-validation
## Further Links 
- https://enterprisecraftsmanship.com/posts/email-uniqueness-as-aggregate-invariant/
- https://enterprisecraftsmanship.com/posts/domain-vs-application-services/
