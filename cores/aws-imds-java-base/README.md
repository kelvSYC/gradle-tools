# AWS IMDS Java Base

A Kotlin library providing managed AWS EC2 Instance Metadata Service (IMDS) client integration using the AWS SDK
for Java, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-imds-java-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `ImdsClientBuildService` | `Ec2MetadataClient` | Synchronous IMDS operations |
| `ImdsAsyncClientBuildService` | `Ec2MetadataAsyncClient` | Asynchronous IMDS operations |

Register a build service:

```kotlin
val imds = gradle.sharedServices.registerIfAbsent("imds", ImdsClientBuildService::class) {
    // both parameters are optional; leave unset for SDK defaults
    parameters.endpoint.set("http://169.254.169.254")
    parameters.endpointMode.set(EndpointMode.IPV4)
}
```

## Value Source: `AbstractImdsValueSource`

Extend `AbstractImdsValueSource` to query any IMDS metadata path and transform the response:

```kotlin
abstract class UpperCaseImdsValueSource
    : AbstractImdsValueSource<String, AbstractImdsValueSource.Parameters>() {

    override fun doObtain(response: Ec2MetadataResponse): String = response.asString().uppercase()
}
```

```kotlin
val instanceType: Provider<String> = providers.of(UpperCaseImdsValueSource::class) {
    parameters {
        service.set(imds)
        path.set("/latest/meta-data/instance-type")
    }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ImdsClientBuildService>` | The shared build service |
| `path` | `Property<String>` | The IMDS metadata path to query |

## Value Source: `ImdsValueSource`

A ready-to-use `AbstractImdsValueSource` that returns the raw IMDS response as a string:

```kotlin
val instanceId: Provider<String> = providers.of(ImdsValueSource::class) {
    parameters {
        service.set(imds)
        path.set("/latest/meta-data/instance-id")
    }
}
```

## Value Source: `AbstractInstanceIdentityValueSource`

Extend `AbstractInstanceIdentityValueSource` to read data from the
[EC2 Instance Identity Document](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html)
and transform it. The document is supplied as a parsed `Document`:

```kotlin
abstract class MyImdsValueSource
    : AbstractInstanceIdentityValueSource<String, AbstractInstanceIdentityValueSource.Parameters>() {

    override fun doObtain(document: Document): String? =
        document.asMap()["accountId"]?.asString()
}
```

```kotlin
tasks.register("readIdentityDoc") {
    val accountId: Provider<String> = providers.of(MyImdsValueSource::class) {
        parameters {
            service.set(imds)
        }
    }

    doLast { println(accountId.get()) }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ImdsClientBuildService>` | The shared build service |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-imds-kotlin-base](../aws-imds-kotlin-base) — Kotlin SDK variant
- [AWS IMDS documentation](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html)
