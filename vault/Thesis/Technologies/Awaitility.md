"Awaitility is a DSL that allows you to express expectations of an asynchronous system in a concise and easy-to-read manner."
```java
@Test
public void updatesCustomerStatus() {
    // Publish an asynchronous message to a broker (e.g. RabbitMQ):
    messageBroker.publishMessage(updateCustomerStatusMessage);
    // Awaitility lets you wait until the asynchronous operation completes:
    await().atMost(5, SECONDS).until(customerStatusIsUpdated());
    ...
}
```
https://github.com/awaitility/awaitility
