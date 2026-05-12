# AWS IMDS Kotlin Base

A Kotlin library providing managed AWS EC2 Instance Metadata Service (IMDS) client integration using the AWS
SDK for Kotlin, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-imds-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `ImdsClientBuildService` | `ImdsClient` (AWS SDK for Kotlin) |

Unlike most AWS service build services, IMDS has no region or credentials — only an optional endpoint
override:

```kotlin
val imds = gradle.sharedServices.registerIfAbsent("imds", ImdsClientBuildService::class) {
    // endpoint is optional; defaults to the AWS SDK default
}
```

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `endpoint` | `Property<String>` | Override the IMDS endpoint URI. Leave unset for `EndpointConfiguration.Default`. |

## Value Source: `AbstractImdsValueSource`

Extend `AbstractImdsValueSource` to query any IMDS metadata path and transform the response:

```kotlin
abstract class UpperCaseImdsValueSource
    : AbstractImdsValueSource<String, AbstractImdsValueSource.Parameters>() {

    override fun doObtain(response: String): String = response.uppercase()
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

`obtain()` returns `null` if the IMDS call throws `EC2MetadataError`.

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ImdsClientBuildService>` | Build service supplying the IMDS client |
| `path` | `Property<String>` | The IMDS metadata path to query |

## Value Source: `ImdsValueSource`

A ready-to-use `AbstractImdsValueSource` that returns the raw IMDS response as a string. Useful for
simple metadata lookups without writing a subclass:

```kotlin
val instanceId: Provider<String> = providers.of(ImdsValueSource::class) {
    parameters {
        service.set(imds)
        path.set("/latest/meta-data/instance-id")
    }
}
```

`obtain()` returns `null` if the IMDS call throws `EC2MetadataError`.

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

`obtain()` returns `null` if the IMDS call throws `EC2MetadataError` (e.g. when not running on EC2).

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ImdsClientBuildService>` | Build service supplying the IMDS client |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-imds-java-base](../aws-imds-java-base) — Java SDK variant with sync/async clients and structured document parsing
- [AWS IMDS documentation](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html)
