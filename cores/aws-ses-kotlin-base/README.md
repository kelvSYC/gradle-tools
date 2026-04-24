# AWS SES Kotlin Base

A Gradle plugin providing managed AWS Simple Email Service (SES) client integration using the AWS SDK for Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-ses-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `SesClientInfo` | `SesClient` (AWS SDK for Kotlin) |

`SesClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<SesClientInfo>("ses") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "ses").asCredentialsProvider)
}
```

## WorkActions

### `SendMailAction`

Sends a plain and/or HTML email via SES:

```kotlin
workerExecutor.noIsolation().submit(SendMailAction::class) {
    service.set(serviceClients.service)
    clientName.set("ses")
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
| `sender` | `Property<String>` | From address |
| `message` | `Property<ByteArray>` | Raw MIME message bytes |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-ses-java-base](../aws-ses-java-base) — Java SDK variant with async client support
