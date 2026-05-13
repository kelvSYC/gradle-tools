# Google Cloud Pub/Sub Base

A Kotlin library providing managed Google Cloud Pub/Sub client integration, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:google-cloud-pubsub-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `TopicAdminClientBuildService` | `TopicAdminClient` |

`TopicAdminClient` supports both topic administration and message publishing.

```kotlin
val pubsub = gradle.sharedServices.registerIfAbsent("pubsub", TopicAdminClientBuildService::class) {
    parameters.credentials.set(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault()))
    // credentials is optional; omit to use application default credentials
}
```

## WorkAction: `PublishAction`

Publishes a single message to a Pub/Sub topic.

```kotlin
workerExecutor.noIsolation().submit(PublishAction::class) {
    service.set(pubsub)
    projectId.set("my-project")
    topicId.set("my-topic")
    data.set("Hello, Pub/Sub!")
    attributes.put("env", "ci")                // optional
    orderingKey.set("partition-1")             // optional
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<TopicAdminClientBuildService>` | The shared build service |
| `projectId` | `Property<String>` | GCP project ID |
| `topicId` | `Property<String>` | Topic ID (short name) |
| `data` | `Property<String>` | Message data (UTF-8 string) |
| `attributes` | `MapProperty<String, String>` | Optional message attributes |
| `orderingKey` | `Property<String>` | Optional ordering key for ordered delivery |

## Task: `PublishBatch`

Publishes an arbitrary number of messages to a topic. Entries are chunked to the Pub/Sub batch limit
(1000 messages) automatically.

```kotlin
tasks.register<PublishBatch>("notify") {
    service.set(pubsub)
    projectId.set("my-project")
    topicId.set("my-topic")
    registerEntry("build-complete") { entry ->
        entry.data.set("""{"status":"success"}""")
        entry.attributes.put("env", "ci")
    }
}
```

Use `AbstractPublishBatch` directly to wire `client` from outside `TopicAdminClientBuildService`.

## Value Source: `ListTopicsValueSource`

Returns the fully-qualified resource names of every topic in a project. Pagination is handled internally.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<TopicAdminClientBuildService>` | The shared build service |
| `projectId` | `Property<String>` | GCP project ID |

## Value Source: `ListTopicSubscriptionsValueSource`

Returns the fully-qualified subscription resource names attached to a given topic.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<TopicAdminClientBuildService>` | The shared build service |
| `projectId` | `Property<String>` | GCP project ID |
| `topicId` | `Property<String>` | Topic ID (short name) |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Google Cloud Pub/Sub Java Client](https://cloud.google.com/java/docs/reference/google-cloud-pubsub/latest/overview)
