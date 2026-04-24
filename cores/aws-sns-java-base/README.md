# AWS SNS Java Base

A Gradle plugin providing managed AWS Simple Notification Service (SNS) client integration using the AWS SDK for Java.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-sns-java-base")
}
```

## Client Types

Two client info types are registered:

| Client info type | Client type | Use case |
|---|---|---|
| `SnsClientInfo` | `SnsClient` | Synchronous SNS operations |
| `SnsAsyncClientInfo` | `SnsAsyncClient` | Asynchronous SNS operations |

Both extend `AwsClientInfo` from `aws-java-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<SnsClientInfo>("sns") {
    region.set(Region.US_EAST_1)
    credentials.set(DefaultCredentialsProvider.create())
}
```

## WorkAction: `PublishAction`

Publishes a message to an SNS topic. Only simple (non-JSON) messages to standard (non-FIFO) topics are supported:

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
| `subject` | `Property<String>` | Optional message subject |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-java-extensions](../aws-java-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-sns-kotlin-base](../aws-sns-kotlin-base) — Kotlin SDK variant
