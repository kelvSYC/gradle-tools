# AWS SQS Java Base

A Gradle plugin providing managed AWS Simple Queue Service (SQS) client integration using the AWS SDK for Java.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-sqs-java-base")
}
```

## Client Types

Two client info types are registered:

| Client info type | Client type | Use case |
|---|---|---|
| `SqsClientInfo` | `SqsClient` | Synchronous SQS operations |
| `SqsAsyncClientInfo` | `SqsAsyncClient` | Asynchronous SQS operations |

Both extend `AwsClientInfo` from `aws-java-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<SqsClientInfo>("sqs") {
    region.set(Region.US_EAST_1)
    credentials.set(DefaultCredentialsProvider.create())
}
```

## WorkAction: `SendMessageAction`

Sends a message to an SQS queue:

```kotlin
workerExecutor.noIsolation().submit(SendMessageAction::class) {
    service.set(serviceClients.service)
    clientName.set("sqs")
    queueUrl.set("https://sqs.us-east-1.amazonaws.com/111122223333/my-queue")
    messageBody.set("Hello from Gradle")
    attributes.put("EventType", MessageAttributeValue.builder()
        .dataType("String")
        .stringValue("BuildComplete")
        .build())   // optional
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `SqsClientInfo` |
| `queueUrl` | `Property<String>` | SQS queue URL |
| `messageBody` | `Property<String>` | Message body |
| `attributes` | `MapProperty<String, MessageAttributeValue>` | Optional message attributes |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-java-extensions](../aws-java-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-sqs-kotlin-base](../aws-sqs-kotlin-base) — Kotlin SDK variant
