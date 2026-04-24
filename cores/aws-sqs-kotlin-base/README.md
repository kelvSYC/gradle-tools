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

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-sqs-java-base](../aws-sqs-java-base) — Java SDK variant with async client support
