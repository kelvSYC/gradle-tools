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
| `subject` | `Property<String>` | Optional message subject (`null` if absent) |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-sns-java-base](../aws-sns-java-base) — Java SDK variant with async client support
