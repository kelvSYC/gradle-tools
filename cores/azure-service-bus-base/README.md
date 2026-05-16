# Azure Service Bus Base

A Kotlin library providing managed Azure Service Bus client integration using the Azure SDK for Java,
built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-service-bus-base")
}
```

## Build Services

### `ServiceBusSenderClientBuildService`

Manages a `ServiceBusSenderClient` scoped to a single queue or topic. Register one instance per
entity you intend to send to.

```kotlin
val sender = gradle.sharedServices.registerIfAbsent("serviceBus", ServiceBusSenderClientBuildService::class) {
    parameters {
        namespace.set("myns.servicebus.windows.net")
        queueName.set("my-queue")   // set this XOR topicName
        // topicName.set("my-topic")
        defaultCredential()
        // managedIdentity()
        // clientSecret(tenantId, clientId, clientSecret)
    }
}
```

The parameter shape extends `AzureBuildServiceParams` from
[azure-extensions](../azure-extensions); use the extension functions on `AzureBuildServiceParams`
to configure credentials atomically. Azure Service Bus only accepts `TokenCredential`-shaped
credentials — attempting to configure with `sasToken()` or `sharedKey()` throws
`IllegalArgumentException` at client construction time. Setting neither or both of `queueName` and
`topicName` also throws at construction time.

| Parameter | Type | Description |
|---|---|---|
| `namespace` | `Property<String>` | Fully-qualified namespace hostname, e.g. `{namespace}.servicebus.windows.net` |
| `queueName` | `Property<String>` | Queue name. Set this XOR `topicName`. |
| `topicName` | `Property<String>` | Topic name. Set this XOR `queueName`. |
| `credentialSource` | `Property<AzureCredentialSource>` | Which credential to construct. Set via the extension functions. |

### `ServiceBusAdministrationClientBuildService`

Manages a `ServiceBusAdministrationClient` for administrative operations such as listing queues,
topics, and subscriptions. Register one instance per namespace.

```kotlin
val admin = gradle.sharedServices.registerIfAbsent("serviceBusAdmin", ServiceBusAdministrationClientBuildService::class) {
    parameters {
        namespace.set("myns.servicebus.windows.net")
        defaultCredential()
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `namespace` | `Property<String>` | Fully-qualified namespace hostname, e.g. `{namespace}.servicebus.windows.net` |
| `credentialSource` | `Property<AzureCredentialSource>` | Which credential to construct. Set via the extension functions. |

## WorkAction: `SendMessageAction`

Sends a single message to a Service Bus queue or topic.

```kotlin
workerExecutor.noIsolation().submit(SendMessageAction::class) {
    service.set(sender)
    body.set("Build complete.")
    subject.set("CI Notification")      // optional
    messageId.set("build-42")           // optional
    sessionId.set("session-a")          // optional — for session-ordered delivery
    partitionKey.set("partition-1")     // optional — for partitioned namespaces
    applicationProperties.put("env", "prod")  // optional
}
```

## Task: `SendMessageBatch`

Sends an arbitrary number of messages using the Service Bus atomic batch API. Messages are
automatically chunked to respect the namespace's per-batch size limit (256 KB for Standard tier,
1 MB for Premium tier).

```kotlin
tasks.register<SendMessageBatch>("notify") {
    service.set(sender)
    registerEntry("module-a") { entry ->
        entry.body.set("Module A built")
    }
    registerEntry("module-b") { entry ->
        entry.body.set("Module B built")
        entry.subject.set("Build update")
        entry.sessionId.set("session-b")
        entry.applicationProperties.put("severity", "info")
    }
}
```

## Value Sources

All value sources require a `ServiceBusAdministrationClientBuildService` registered separately.

### `ListQueuesValueSource`

Returns a list of queue names within the namespace.

```kotlin
val queues: Provider<List<String>> = providers.of(ListQueuesValueSource::class) {
    parameters {
        service.set(admin)
    }
}
```

### `ListTopicsValueSource`

Returns a list of topic names within the namespace.

```kotlin
val topics: Provider<List<String>> = providers.of(ListTopicsValueSource::class) {
    parameters {
        service.set(admin)
    }
}
```

### `ListSubscriptionsValueSource`

Returns a list of subscription names for a given topic.

```kotlin
val subscriptions: Provider<List<String>> = providers.of(ListSubscriptionsValueSource::class) {
    parameters {
        service.set(admin)
        topicName.set("my-topic")
    }
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [azure-extensions](../azure-extensions) — Azure credential configuration
- [azure-key-vault-base](../azure-key-vault-base) — Azure Key Vault variant
