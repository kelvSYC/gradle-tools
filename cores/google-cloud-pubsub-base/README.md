# Google Cloud Pub/Sub Base

A Gradle plugin providing managed Google Cloud Pub/Sub client integration.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.google-cloud-pubsub-base")
}
```

## Client Registration

The client info type is `PubSubClientInfo`. The registered client is a `TopicAdminClient`, which supports
both topic administration and message publishing.

```kotlin
the<ClientsBaseExtension>().service.get()
    .registerIfAbsent<PubSubClientInfo>("myPubSub") {
        credentials.set(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault()))
    }
```

### `PubSubClientInfo` properties

| Property | Type | Description |
|---|---|---|
| `credentials` | `Property<CredentialsProvider>` | Google API `CredentialsProvider` for authentication. Required. |

## Work Action: `PublishAction`

Publishes a single message to a Pub/Sub topic.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `PubSubClientInfo` |
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
    clientName.set("myPubSub")
    projectId.set("my-project")
    topicId.set("my-topic")
    registerEntry("build-complete") {
        data.set("""{"status":"success"}""")
        attributes.put("env", "ci")
    }
}
```

## Value Source: `ListTopicsValueSource`

Returns the fully-qualified resource names of every topic in a project. Pagination is handled internally.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `PubSubClientInfo` |
| `projectId` | `Property<String>` | GCP project ID |

## Value Source: `ListTopicSubscriptionsValueSource`

Returns the fully-qualified subscription resource names attached to a given topic.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `PubSubClientInfo` |
| `projectId` | `Property<String>` | GCP project ID |
| `topicId` | `Property<String>` | Topic ID (short name) |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Google Cloud Pub/Sub Java Client](https://cloud.google.com/java/docs/reference/google-cloud-pubsub/latest/overview)
