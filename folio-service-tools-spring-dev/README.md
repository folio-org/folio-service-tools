# Documentation for folio-service-tools-spring-dev features

## System user creation and utilization

### Creation

If module need system user to communicate with other modules then it's required to create the system
user on enabling for tenant. To do so you should:

1. Extend `TenantService` from folio-spring-base
2. Inject `PrepareSystemUserService` bean to the class
3. Override `afterTenantUpdate` and use `setupSystemUser()` from injected service

Requirements:

* Prepare file with permissions that should be assigned to the user (one permission per line)
* Set-up application properties:
    * folio.okapi-url (suggested to pass it from environment variables)
    * folio.system-user.username (suggested to have the same name as module name)
    * folio.system-user.password (suggested to pass it from environment variables)
    * folio.system-user.lastname (suggested to set it to `System`)
    * folio.system-user.permissionsFilePath (path to prepared permissions-file in resources folder)
* Add `spring-boot-starter-cache` dependency to module if you want to cache system user authentication data
* Update ModuleDescriptor with modulePermissions for `POST /_/tenant` endpoint:
    * users.collection.get
    * users.item.post
    * login.item.post
    * perms.users.get
    * perms.users.item.post
    * perms.users.assign.immutable
    * perms.users.assign.mutable
* Update ModuleDescriptor with requires interfaces:
    * login
    * permissions
    * users

### Utilization

If system user was created during enabling for tenant, then the system user could be used to make request
to other modules. To do so `SystemUserScopedExecutionService` could be used.

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


