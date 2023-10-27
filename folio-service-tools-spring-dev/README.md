# Documentation for folio-service-tools-spring-dev features

## Kafka topic creation
In order to make module create topics for Kafka, topic parameters should be added to application properties file with
`folio.kafka.topics` path. 

### Topic parameters:
| Property name     | Description                                                                                              |
|-------------------|----------------------------------------------------------------------------------------------------------|
| name              | Topic base name that will be concatenated with environment name and tenant name.                         |
| numPartitions     | Break a topic into multiple partitions. Can be left blank in order to use default '-1' value.            |
| replicationFactor | Specify how much replicas do you need for a topic. Can be left blank in order to use default '-1' value. |

Also, `folio.environment` should be added to application properties. (example: `folio.environment: ${ENV:folio}`)

Then next actions are requires:
1. Extend `TenantService` from folio-spring-base
2. Inject `KafkaAdminService` bean to the class
3. Override `afterTenantUpdate` and use `createTopics()` for topics creations and `restartEventListeners()` for 
restarting Kafka event listeners in module

## Retry mechanism for batch processing

`MessageBatchProcessor` consumes batch of values as list and tries to process them using the strategy with retry.
At first, a batch will be retried by the specified retry policy, then, if it's failing, it would be processed by single value at one time, if the value would be failed to process - failedValueConsumer will be executed.

`MessageBatchProcessor` requires RetryTemplate bean name to be specified in `consumeBatchWithFallback` method.
Configure your own `RetryTemplate` bean or use already defined default beans.
To make `MessageBatchProcessor` configured and created as Spring bean `folio.retry.enabled = true` should be specified in application properties.

Default retry policies:

| RetryTemplate bean                | Attempts | Backoff | Attempts config property            | Backoff config proerty        |
|-----------------------------------|----------|---------|-------------------------------------|-------------------------------|
| DEFAULT_RETRY_TEMPLATE_NAME       | 5        | 1000    | -                                   | -                             |
| DEFAULT_KAFKA_RETRY_TEMPLATE_NAME | 5        | 1000    | folio.kafka.retry-delivery-attempts | folio.kafka.retry-interval-ms |


