# AWS SQS Kotlin Base

A Kotlin library providing managed AWS Simple Queue Service (SQS) client integration using the AWS SDK for Kotlin,
built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-sqs-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `SqsClientBuildService` | `SqsClient` (AWS SDK for Kotlin) |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val sqs = gradle.sharedServices.registerIfAbsent("sqs", SqsClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        from(providers.credentials(AwsCredentials::class.java, "sqs"))
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

## WorkAction: `SendMessageAction`

Sends a single message to an SQS queue:

```kotlin
workerExecutor.noIsolation().submit(SendMessageAction::class) {
    service.set(sqs)
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
| `service` | `Property<SqsClientBuildService>` | The shared build service |
| `queueUrl` | `Property<String>` | SQS queue URL |
| `messageBody` | `Property<String>` | Message body |
| `attributes` | `MapProperty<String, MessageAttributeValue>` | Optional message attributes |
| `messageGroupId` | `Property<String>` | FIFO queues only: required group id |
| `messageDeduplicationId` | `Property<String>` | FIFO queues only: required when content-based deduplication is disabled |

For FIFO queues, set `messageGroupId` (and `messageDeduplicationId` if the queue has content-based
deduplication disabled):

```kotlin
workerExecutor.noIsolation().submit(SendMessageAction::class) {
    service.set(sqs)
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
    service.set(sqs)
    queueUrl.set("https://sqs.us-east-1.amazonaws.com/111122223333/my-queue")

    registerEntry("module-a") { entry ->
        entry.messageBody.set("Module A built")
    }
    registerEntry("module-b") { entry ->
        entry.messageBody.set("Module B built")
        entry.attributes.put("Severity", MessageAttributeValue {
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
`AbstractSendMessageBatch` directly to wire `client` from outside `SqsClientBuildService`.

| Property | Type | Description |
|---|---|---|
| `service` | `Property<SqsClientBuildService>` | The shared build service |
| `queueUrl` | `Property<String>` | SQS queue URL |
| Entry: `messageBody` | `Property<String>` | Message body |
| Entry: `attributes` | `MapProperty<String, MessageAttributeValue>` | Optional message attributes |
| Entry: `messageGroupId` | `Property<String>` | FIFO queues only |
| Entry: `messageDeduplicationId` | `Property<String>` | FIFO queues only |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-sqs-java-base](../aws-sqs-java-base) — Java SDK variant with async client support
