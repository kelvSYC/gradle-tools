# AWS SNS Kotlin Base

A Kotlin library providing managed AWS Simple Notification Service (SNS) client integration using the AWS SDK for
Kotlin, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-sns-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `SnsClientBuildService` | `SnsClient` (AWS SDK for Kotlin) |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val sns = gradle.sharedServices.registerIfAbsent("sns", SnsClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        from(providers.credentials(AwsCredentials::class.java, "sns"))
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

## Task: `Publish`

Publishes a single message to an SNS topic. Only simple (non-JSON) messages are supported:

```kotlin
tasks.register<Publish>("publish") {
    service.set(sns)
    topicArn.set("arn:aws:sns:us-east-1:111122223333:my-topic")
    message.set("Build complete.")
    subject.set("CI Notification")    // optional
}
```

| Property | Type | Description |
|---|---|---|
| `service` | `Property<SnsClientBuildService>` | The shared build service |
| `topicArn` | `Property<String>` | SNS topic ARN |
| `message` | `Property<String>` | Message body (sent to all transport protocols) |
| `subject` | `Property<String>` | Optional message subject (`null` if absent) |
| `messageGroupId` | `Property<String>` | FIFO topics only: required group id |
| `messageDeduplicationId` | `Property<String>` | FIFO topics only: required when content-based deduplication is disabled |

For FIFO topics, set `messageGroupId` (and `messageDeduplicationId` if the topic has content-based
deduplication disabled):

```kotlin
tasks.register<Publish>("publishFifo") {
    service.set(sns)
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
    service.set(sns)
    topicArn.set("arn:aws:sns:us-east-1:111122223333:my-topic")

    registerEntry("module-a") { entry ->
        entry.message.set("Module A built")
    }
    registerEntry("module-b") { entry ->
        entry.message.set("Module B built")
        entry.subject.set("Build update")
        entry.attributes.put("Severity", MessageAttributeValue {
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
`AbstractPublishBatch` directly to wire `client` from outside `SnsClientBuildService`.

| Property | Type | Description |
|---|---|---|
| `service` | `Property<SnsClientBuildService>` | The shared build service |
| `topicArn` | `Property<String>` | SNS topic ARN |
| Entry: `message` | `Property<String>` | Message body |
| Entry: `subject` | `Property<String>` | Optional subject |
| Entry: `attributes` | `MapProperty<String, MessageAttributeValue>` | Optional message attributes |
| Entry: `messageGroupId` | `Property<String>` | FIFO topics only |
| Entry: `messageDeduplicationId` | `Property<String>` | FIFO topics only |

## Why no WorkActions

The AWS Kotlin SDK exposes all service calls as `suspend` functions. A `WorkAction` that wraps a single suspend call reduces to:

```kotlin
override fun execute() {
    runBlocking { singleSuspendCall() }
}
```

This adds ceremony with no benefit: no return values, no isolation beyond what coroutines already provide, and no concurrency advantage (Gradle's task graph handles cross-task concurrency; coroutines handle within-task concurrency). WorkActions were designed for blocking Java SDK calls to avoid tying up Gradle's worker thread pool — that problem doesn't exist with a coroutine-based SDK.

Accordingly, this component exposes `DefaultTask` subclasses instead. Plugin authors needing compound operations should compose via Gradle task dependencies (sequential) or call `service.get().getClient()` directly inside a `runBlocking { coroutineScope { } }` block (parallel).

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-sns-java-base](../aws-sns-java-base) — Java SDK variant with async client support
