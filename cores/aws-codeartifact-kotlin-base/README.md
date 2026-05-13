# AWS CodeArtifact Kotlin Base

A Kotlin library providing managed AWS CodeArtifact client integration using the AWS SDK for Kotlin, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-codeartifact-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `CodeArtifactClientBuildService` | `CodeartifactClient` (AWS SDK for Kotlin) |

```kotlin
val codeArtifact = gradle.sharedServices.registerIfAbsent("ca", CodeArtifactClientBuildService::class) {
    parameters.region.set("us-east-1")
    parameters.credentials.set(providers.credentials(AwsCredentials::class.java, "ca").asCredentialsProvider)
}
```

## Value Source: `GetAuthorizationTokenValueSource`

Retrieves an AWS CodeArtifact authorization token:

```kotlin
val token: Provider<String> = providers.of(GetAuthorizationTokenValueSource::class) {
    parameters {
        service.set(codeArtifact)
        domain.set("my-domain")
        domainOwner.set("111122223333")
        duration.set(3600L)
    }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<CodeArtifactClientBuildService>` | Build service supplying the CodeArtifact client |
| `domain` | `Property<String>` | CodeArtifact domain name |
| `domainOwner` | `Property<String>` | AWS account ID owning the domain |
| `duration` | `Property<Long>` | Token validity in seconds |

## Value Source: `GetRepositoryEndpointValueSource`

Retrieves the endpoint URL for a CodeArtifact repository:

```kotlin
val endpoint: Provider<String> = providers.of(GetRepositoryEndpointValueSource::class) {
    parameters {
        service.set(codeArtifact)
        domain.set("my-domain")
        domainOwner.set("111122223333")
        repository.set("my-repo")
        endpointType.set(EndpointType.Ipv4.value)       // optional, defaults to Ipv4
        format.set(PackageFormat.Maven.value)           // optional, defaults to Generic
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

## Value Source: `ListPackageVersionsValueSource`

Lists all version strings for a package in a CodeArtifact repository, paginating automatically:

```kotlin
val versions: Provider<List<String>> = providers.of(ListPackageVersionsValueSource::class) {
    parameters {
        service.set(codeArtifact)
        domain.set("my-domain")
        domainOwner.set("111122223333")
        repository.set("my-repo")
        format.set("generic")
        namespace.set("my-namespace")
        packageValue.set("my-package")
    }
}
```

## WorkAction: `GetGenericPackageVersionAssetAction`

Downloads a CodeArtifact generic repository asset to a file:

```kotlin
workerExecutor.noIsolation().submit(GetGenericPackageVersionAssetAction::class) {
    service.set(codeArtifact)
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

## WorkAction: `PublishPackageVersionAction`

Publishes an asset to a CodeArtifact generic package version:

```kotlin
workerExecutor.noIsolation().submit(PublishPackageVersionAction::class) {
    service.set(codeArtifact)
    domain.set("my-domain")
    domainOwner.set("111122223333")
    repository.set("my-repo")
    namespace.set("my-namespace")
    packageValue.set("my-package")
    packageVersion.set("1.0.0")
    assetName.set("my-asset.jar")
    assetSHA256.set("abc123...")
    assetContent.set(layout.buildDirectory.file("artifacts/my-asset.jar"))
    unfinished.set(false) // optional; set true when uploading multiple assets
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-codeartifact-java-base](../aws-codeartifact-java-base) — Java SDK variant with async client and structured asset access
