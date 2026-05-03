# AWS SQS Kotlin Base

A Gradle plugin providing managed AWS Simple Queue Service (SQS) client integration using the AWS SDK for Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-sqs-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `SqsClientInfo` | `SqsClient` (AWS SDK for Kotlin) |

`SqsClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<SqsClientInfo>("sqs") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "sqs").asCredentialsProvider)
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
    attributes.put("EventType", MessageAttributeValue {
        dataType = "String"
        stringValue = "BuildComplete"
    })   // optional
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `SqsClientInfo` |
| `queueUrl` | `Property<String>` | SQS queue URL |
| `messageBody` | `Property<String>` | Message body |
| `attributes` | `MapProperty<String, MessageAttributeValue>` | Optional message attributes |
| `messageGroupId` | `Property<String>` | FIFO queues only: required group id |
| `messageDeduplicationId` | `Property<String>` | FIFO queues only: required when content-based deduplication is disabled |

For FIFO queues, set `messageGroupId` (and `messageDeduplicationId` if the queue has content-based
deduplication disabled):

```kotlin
workerExecutor.noIsolation().submit(SendMessageAction::class) {
    service.set(serviceClients.service)
    clientName.set("sqs")
    queueUrl.set("https://sqs.us-east-1.amazonaws.com/111122223333/my-queue.fifo")
    messageBody.set("Hello from Gradle")
    messageGroupId.set("build-events")
    messageDeduplicationId.set("build-${project.version}")
}
```

## Task: `SendMessageBatch`

Sends an arbitrary number of messages to a queue in a single task. Entries are submitted via SQS
`SendMessageBatch`; the task internally chunks entries into the maximum batch size supported by SQS, so
callers may register any number of entries.

```kotlin
tasks.register<SendMessageBatch>("notify") {
    clientName.set("sqs")
    queueUrl.set("https://sqs.us-east-1.amazonaws.com/111122223333/my-queue")

    registerEntry("module-a") {
        messageBody.set("Module A built")
    }
    registerEntry("module-b") {
        messageBody.set("Module B built")
        attributes.put("Severity", MessageAttributeValue {
            dataType = "String"
            stringValue = "info"
        })
    }
}
```

Each entry's name is used as the SQS batch entry id and must be unique within the task. The task fails if
any chunk's API call fails or if SQS reports per-entry failures (the failure message lists the failed
entry ids).

For FIFO queues, set `messageGroupId` (and `messageDeduplicationId` if needed) on each entry. Use
`AbstractSendMessageBatch` directly to wire `client` from outside `ClientsBaseService`.

| Property | Type | Description |
|---|---|---|
| `clientName` | `Property<String>` | Registered name of a `SqsClientInfo` |
| `queueUrl` | `Property<String>` | SQS queue URL |
| Entry: `messageBody` | `Property<String>` | Message body |
| Entry: `attributes` | `MapProperty<String, MessageAttributeValue>` | Optional message attributes |
| Entry: `messageGroupId` | `Property<String>` | FIFO queues only |
| Entry: `messageDeduplicationId` | `Property<String>` | FIFO queues only |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-sqs-java-base](../aws-sqs-java-base) — Java SDK variant with async client support
