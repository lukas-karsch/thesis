1. Frontend sends Command via REST.
2. Backend issues Command.
3. Backend _simultaneously_ subscribes to a query update on the projection.
4. When the projection updates, the Backend returns `201 CREATED` to the Frontend.
By doing this, the ES system looks synchronous to the outside world.
-> do I want this? 
## Links
- Axon Docs: https://www.axoniq.io/blog/introducing-subscription-queries
- Example app https://github.com/fransvanbuul/gc-subscriptions
- Creating a synchronous REST frontend with Axon: https://github.com/fransvanbuul/axon-sync-rest-frontend/tree/master