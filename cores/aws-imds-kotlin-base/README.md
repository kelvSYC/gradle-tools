# AWS IMDS Kotlin Base

A Gradle plugin providing managed AWS EC2 Instance Metadata Service (IMDS) client integration using the AWS SDK for
Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-imds-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `ImdsClientInfo` | `ImdsClient` (AWS SDK for Kotlin) |

Register a client using `serviceClients`:

```kotlin
serviceClients.service.get().registerIfAbsent<ImdsClientInfo>("imds") {
    // endpoint is optional; defaults to the AWS SDK default
}
```

### `ImdsClientInfo` properties

| Property | Type | Description |
|---|---|---|
| `endpoint` | `Property<String>` | Override the IMDS endpoint URI. Leave unset for the default. |

Note: unlike the Java variant, the Kotlin SDK's `ImdsClient` does not expose a separate `endpointMode` property.
The endpoint configuration is derived from `endpoint` via `EndpointConfiguration.Custom`.

## Value Source: `AbstractInstanceIdentityValueSource`

Extend `AbstractInstanceIdentityValueSource` to read data from the
[EC2 Instance Identity Document](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html)
and transform it. The document is returned as a raw JSON string:

```kotlin
abstract class MyImdsValueSource
    : AbstractInstanceIdentityValueSource<String, AbstractInstanceIdentityValueSource.Parameters>() {

    override fun doObtain(document: String): String? =
        document  // parse as needed, e.g. with kotlinx.serialization
}
```

Use it in task configuration:

```kotlin
tasks.register("readIdentityDoc") {
    val doc: Provider<String> = providers.of(MyImdsValueSource::class) {
        parameters {
            service.set(serviceClients.service)
            clientName.set("imds")
        }
    }

    doLast {
        println(doc.get())
    }
}
```

`obtain()` returns `null` if the IMDS call throws `EC2MetadataError` (e.g. when not running on EC2).

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service (set from `serviceClients.service`) |
| `clientName` | `Property<String>` | Registered name of an `ImdsClientInfo` |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-imds-java-base](../aws-imds-java-base) — Java SDK variant with sync/async clients and structured document parsing
- [AWS IMDS documentation](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html)
