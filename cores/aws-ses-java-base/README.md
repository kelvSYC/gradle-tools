# AWS SES Java Base

A Kotlin library providing managed AWS Simple Email Service (SES) client integration using the AWS SDK for Java,
built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-ses-java-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `SesClientBuildService` | `SesClient` | Synchronous SES operations |
| `SesAsyncClientBuildService` | `SesAsyncClient` | Asynchronous SES operations |

Register a build service from a plugin or `build.gradle.kts`:

```kotlin
val ses = gradle.sharedServices.registerIfAbsent("ses", SesClientBuildService::class) {
    parameters.region.set(Region.US_EAST_1)
    parameters.credentials.set(DefaultCredentialsProvider.create())
}
```

Both parameters are optional. Leave `region` unset to fall back to the SDK's `DefaultAwsRegionProviderChain`,
and leave `credentials` unset to fall back to anonymous credentials.

## WorkActions

### `SendMailAction`

Sends a plain and/or HTML email via SES:

```kotlin
workerExecutor.noIsolation().submit(SendMailAction::class) {
    service.set(ses)
    sender.set("no-reply@example.com")
    recipients.add("user@example.com")
    ccAddresses.add("cc@example.com")     // optional
    bccAddresses.add("bcc@example.com")   // optional
    subject.set("Build notification")
    htmlMessage.set("<p>Build complete.</p>")   // optional
    textMessage.set("Build complete.")          // optional
}
```

### `AbstractSendTemplatedMailAction`

Extend this class to send a templated SES email. Subclasses must define a concrete `Parameters` interface extending
`AbstractSendTemplatedMailAction.Parameters` and supply `templateJson`:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<SesClientBuildService>` | The shared build service |
| `sender` | `Property<String>` | From address |
| `recipients` | `ListProperty<String>` | To addresses |
| `ccAddresses` | `ListProperty<String>` | CC addresses (optional) |
| `bccAddresses` | `ListProperty<String>` | BCC addresses (optional) |
| `templateName` | `Property<String>` | SES template name |
| `templateJson` | `Property<String>` | JSON string of template substitution values |

### `AbstractSendRawMailAction`

Extend this class to send a raw MIME email. Subclasses must define a concrete `Parameters` interface extending
`AbstractSendRawMailAction.Parameters` and supply `message`:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<SesClientBuildService>` | The shared build service |
| `sender` | `Property<String>` | From address |
| `message` | `Property<ByteArray>` | Raw MIME message bytes |

## Tasks

### `SendBulkTemplatedMail`

Sends a templated email to multiple destinations via the SES `SendBulkTemplatedEmail` API. Internally chunks
entries into the maximum batch size (50) supported by SES:

```kotlin
tasks.register<SendBulkTemplatedMail>("notifyAll") {
    service.set(ses)
    sender.set("no-reply@example.com")
    templateName.set("build-report")
    defaultTemplateData.set("{\"project\":\"my-project\"}")   // optional fallback
    registerEntry("user1") { entry ->
        entry.recipients.set(listOf("user1@example.com"))
        entry.templateData.set("{\"project\":\"my-project\",\"status\":\"success\"}")
    }
    registerEntry("user2") { entry ->
        entry.recipients.set(listOf("user2@example.com"))
        entry.ccAddresses.set(listOf("manager@example.com"))
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<SesClientBuildService>` | The shared build service |
| `sender` | `Property<String>` | From address |
| `templateName` | `Property<String>` | SES template name |
| `defaultTemplateData` | `Property<String>` | Default template data JSON (optional) |

Each entry accepts:

| Property | Type | Description |
|---|---|---|
| `recipients` | `ListProperty<String>` | To addresses |
| `ccAddresses` | `ListProperty<String>` | CC addresses (optional) |
| `bccAddresses` | `ListProperty<String>` | BCC addresses (optional) |
| `templateData` | `Property<String>` | Per-destination replacement template data JSON (optional) |

Use `AbstractSendBulkTemplatedMail` directly to wire `client` from outside `SesClientBuildService`.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-ses-kotlin-base](../aws-ses-kotlin-base) — Kotlin SDK variant
