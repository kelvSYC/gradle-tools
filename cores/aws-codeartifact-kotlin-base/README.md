# AWS CodeArtifact Kotlin Base

A Gradle plugin providing managed AWS CodeArtifact client integration using the AWS SDK for Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-codeartifact-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `CodeArtifactClientInfo` | `CodeartifactClient` (AWS SDK for Kotlin) |

`CodeArtifactClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<CodeArtifactClientInfo>("myCodeArtifact") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "myCodeArtifact").asCredentialsProvider)
}
```

## Value Source: `GetAuthorizationTokenValueSource`

Retrieves an AWS CodeArtifact authorization token using `runBlocking`:

```kotlin
val token: Provider<String> = providers.of(GetAuthorizationTokenValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("myCodeArtifact")
        domain.set("my-domain")
        domainOwner.set("111122223333")
        duration.set(3600L)
    }
}
```

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
        endpointType.set(EndpointType.Ipv4)       // optional, defaults to Ipv4
        format.set(PackageFormat.Maven)           // optional, defaults to Generic
    }
}
```

## Value Source: `AbstractGetGenericAssetValueSource`

Extend this class to read an asset from a CodeArtifact generic repository into memory:

```kotlin
abstract class MyAssetValueSource
    : AbstractGetGenericAssetValueSource<String, AbstractGetGenericAssetValueSource.Parameters>() {

    override fun doObtain(response: GetPackageVersionAssetResponse): String? =
        response.asset?.decodeToString()
}
```

Parameters mirror those of the Java variant (see [aws-codeartifact-java-base](../aws-codeartifact-java-base)).

## WorkAction: `GetGenericPackageVersionAssetAction`

Downloads a CodeArtifact generic repository asset to a file:

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
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-codeartifact-java-base](../aws-codeartifact-java-base) — Java SDK variant with async client and structured asset access
