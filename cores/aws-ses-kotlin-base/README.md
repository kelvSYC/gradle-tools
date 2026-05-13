# AWS SES Kotlin Base

A Kotlin library providing managed AWS Simple Email Service (SES) client integration using the AWS SDK for Kotlin,
built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-ses-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `SesClientBuildService` | `SesClient` (AWS SDK for Kotlin) |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val ses = gradle.sharedServices.registerIfAbsent("ses", SesClientBuildService::class) {
    parameters.region.set("us-east-1")
    parameters.credentials.set(providers.credentials(AwsCredentials::class.java, "ses").asCredentialsProvider)
}
```

Both parameters are optional. Leave `region` unset to fall back to the AWS SDK for Kotlin default region provider
chain, and leave `credentials` unset to fall back to the default credentials provider chain.

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
- [aws-ses-java-base](../aws-ses-java-base) — Java SDK variant with async client support
