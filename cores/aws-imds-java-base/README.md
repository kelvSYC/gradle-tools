# AWS IMDS Java Base

A Gradle plugin providing managed AWS EC2 Instance Metadata Service (IMDS) client integration using the AWS SDK for
Java.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-imds-java-base")
}
```

## Client Types

Two client info types are registered:

| Client info type | Client type | Use case |
|---|---|---|
| `ImdsClientInfo` | `Ec2MetadataClient` | Synchronous IMDS access |
| `ImdsAsyncClientInfo` | `Ec2MetadataAsyncClient` | Asynchronous IMDS access |

Register clients using `serviceClients`:

```kotlin
serviceClients.service.get().registerIfAbsent<ImdsClientInfo>("imds") {
    // endpoint and endpointMode are optional; defaults match the AWS SDK defaults
}
```

### Client info properties

| Property | Type | Description |
|---|---|---|
| `endpoint` | `Property<String>` | Override the IMDS endpoint URI. Leave unset for the default. |
| `endpointMode` | `Property<EndpointMode>` | Override the endpoint mode (`IPv4` or `IPv6`). Leave unset for the default. |

## Value Source: `AbstractInstanceIdentityValueSource`

Extend `AbstractInstanceIdentityValueSource` to read data from the
[EC2 Instance Identity Document](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html)
and transform it:

```kotlin
abstract class MyImdsValueSource
    : AbstractInstanceIdentityValueSource<String, AbstractInstanceIdentityValueSource.Parameters>() {

    override fun doObtain(document: Document): String? =
        document.asMap()["instanceId"]?.asString()
}
```

Use it in task configuration:

```kotlin
tasks.register("readInstanceId") {
    val instanceId: Provider<String> = providers.of(MyImdsValueSource::class) {
        parameters {
            service.set(serviceClients.service)
            clientName.set("imds")
        }
    }

    doLast {
        println("Instance ID: ${instanceId.get()}")
    }
}
```

The `doObtain` method receives a `software.amazon.awssdk.core.document.Document` parsed from the JSON identity
document at `/latest/dynamic/instance-identity/document`.

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service (set from `serviceClients.service`) |
| `clientName` | `Property<String>` | Registered name of an `ImdsClientInfo` |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [AWS IMDS documentation](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html)
