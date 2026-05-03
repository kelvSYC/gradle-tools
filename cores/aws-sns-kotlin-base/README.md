# AWS SNS Kotlin Base

A Gradle plugin providing managed AWS Simple Notification Service (SNS) client integration using the AWS SDK for
Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-sns-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `SnsClientInfo` | `SnsClient` (AWS SDK for Kotlin) |

`SnsClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<SnsClientInfo>("sns") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "sns").asCredentialsProvider)
}
```

## WorkAction: `PublishAction`

Publishes a message to an SNS topic. Only simple (non-JSON) messages are supported:

```kotlin
workerExecutor.noIsolation().submit(PublishAction::class) {
    service.set(serviceClients.service)
    clientName.set("sns")
    topicArn.set("arn:aws:sns:us-east-1:111122223333:my-topic")
    message.set("Build complete.")
    subject.set("CI Notification")    // optional
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `SnsClientInfo` |
| `topicArn` | `Property<String>` | SNS topic ARN |
| `message` | `Property<String>` | Message body (sent to all transport protocols) |
| `subject` | `Property<String>` | Optional message subject (`null` if absent) |
| `messageGroupId` | `Property<String>` | FIFO topics only: required group id |
| `messageDeduplicationId` | `Property<String>` | FIFO topics only: required when content-based deduplication is disabled |

For FIFO topics, set `messageGroupId` (and `messageDeduplicationId` if the topic has content-based
deduplication disabled):

```kotlin
workerExecutor.noIsolation().submit(PublishAction::class) {
    service.set(serviceClients.service)
    clientName.set("sns")
    topicArn.set("arn:aws:sns:us-east-1:111122223333:my-topic.fifo")
    message.set("Build complete.")
    messageGroupId.set("build-events")
    messageDeduplicationId.set("build-${project.version}")
}
```

## Task: `PublishBatch`

Publishes an arbitrary number of messages to a topic in a single task. Entries are submitted via SNS
`PublishBatch`; the task internally chunks entries into the maximum batch size supported by SNS, so
callers may register any number of entries.

```kotlin
tasks.register<PublishBatch>("notify") {
    clientName.set("sns")
    topicArn.set("arn:aws:sns:us-east-1:111122223333:my-topic")

    registerEntry("module-a") {
        message.set("Module A built")
    }
    registerEntry("module-b") {
        message.set("Module B built")
        subject.set("Build update")
        attributes.put("Severity", MessageAttributeValue {
            dataType = "String"
            stringValue = "info"
        })
    }
}
```

Each entry's name is used as the SNS batch entry id and must be unique within the task. The task fails if
any chunk's API call fails or if SNS reports per-entry failures (the failure message lists the failed
entry ids).

For FIFO topics, set `messageGroupId` (and `messageDeduplicationId` if needed) on each entry. Use
`AbstractPublishBatch` directly to wire `client` from outside `ClientsBaseService`.

| Property | Type | Description |
|---|---|---|
| `clientName` | `Property<String>` | Registered name of a `SnsClientInfo` |
| `topicArn` | `Property<String>` | SNS topic ARN |
| Entry: `message` | `Property<String>` | Message body |
| Entry: `subject` | `Property<String>` | Optional subject |
| Entry: `attributes` | `MapProperty<String, MessageAttributeValue>` | Optional message attributes |
| Entry: `messageGroupId` | `Property<String>` | FIFO topics only |
| Entry: `messageDeduplicationId` | `Property<String>` | FIFO topics only |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-sns-java-base](../aws-sns-java-base) — Java SDK variant with async client support
