# AWS CodeArtifact Java Base

A Gradle plugin providing managed AWS CodeArtifact client integration using the AWS SDK for Java.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-codeartifact-java-base")
}
```

## Client Types

Two client info types are registered:

| Client info type | Client type | Use case |
|---|---|---|
| `CodeArtifactClientInfo` | `CodeartifactClient` | Synchronous CodeArtifact operations |
| `CodeArtifactAsyncClientInfo` | `CodeartifactAsyncClient` | Asynchronous CodeArtifact operations |

`CodeArtifactClientInfo` extends `AwsClientInfo` from `aws-java-extensions`.
`CodeArtifactAsyncClientInfo` declares `region: Property<Region>` and `credentials: Property<AwsCredentialsProvider>`
directly. Both default to `AnonymousCredentialsProvider` if credentials are absent.

Use the convenience extensions to register clients:

```kotlin
serviceClients.registerAwsCodeArtifactJavaClient("myCodeArtifact") {
    region.set(Region.US_EAST_1)
    credentials.set(DefaultCredentialsProvider.create())
}
```

## Value Source: `GetAuthorizationTokenValueSource`

Retrieves an AWS CodeArtifact authorization token:

```kotlin
val token: Provider<String> = providers.of(GetAuthorizationTokenValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("myCodeArtifact")
        domain.set("my-domain")
        domainOwner.set("111122223333")
        duration.set(3600L)   // seconds; required
    }
}
```

Returns `null` if the call throws `CodeartifactException`.

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `CodeArtifactClientInfo` |
| `domain` | `Property<String>` | CodeArtifact domain name |
| `domainOwner` | `Property<String>` | AWS account ID owning the domain |
| `duration` | `Property<Long>` | Token validity in seconds |

## Value Source: `GetRepositoryEndpointValueSource`

Retrieves the endpoint URL for a CodeArtifact repository:

```kotlin
val endpoint: Provider<String> = providers.of(GetRepositoryEndpointValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("myCodeArtifact")
        domain.set("my-domain")
        domainOwner.set("111122223333")
        repository.set("my-repo")
        endpointType.set(EndpointType.IPV4)    // optional, defaults to IPV4
        format.set(PackageFormat.MAVEN)        // optional, defaults to GENERIC
    }
}
```

Returns `null` if the call throws `CodeartifactException`.

## Value Source: `AbstractGetGenericAssetValueSource`

Extend this class to read an asset from a CodeArtifact generic repository into memory:

```kotlin
abstract class MyAssetValueSource
    : AbstractGetGenericAssetValueSource<String, AbstractGetGenericAssetValueSource.Parameters>() {

    override fun doObtain(response: GetPackageVersionAssetResponse, input: AbortableInputStream): String? =
        input.bufferedReader().use { it.readText() }
}
```

Parameters extend `AbstractGetGenericAssetValueSource.Parameters`:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `CodeArtifactClientInfo` |
| `domain` | `Property<String>` | CodeArtifact domain name |
| `domainOwner` | `Property<String>` | AWS account ID owning the domain |
| `repository` | `Property<String>` | Repository name |
| `namespace` | `Property<String>` | Package namespace |
| `packageValue` | `Property<String>` | Package name |
| `packageVersion` | `Property<String>` | Package version |
| `asset` | `Property<String>` | Asset name |

## WorkAction: `GetGenericPackageVersionAssetAction`

Downloads a CodeArtifact generic repository asset to a file. Submit via `WorkerExecutor`:

```kotlin
workerExecutor.noIsolation().submit(GetGenericPackageVersionAssetAction::class) {
    service.set(serviceClients.service)
    clientName.set("myCodeArtifact")
    domain.set("my-domain")
    domainOwner.set("111122223333")
    repository.set("my-repo")
    namespace.set("my-namespace")
    packageValue.set("my-package")
    packageVersion.set("1.0.0")
    asset.set("my-package-1.0.0.jar")
    outputFile.set(layout.buildDirectory.file("downloads/my-package-1.0.0.jar"))
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-java-extensions](../aws-java-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-codeartifact-kotlin-base](../aws-codeartifact-kotlin-base) — Kotlin SDK variant
