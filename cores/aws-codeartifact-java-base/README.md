# AWS CodeArtifact Java Base

A Kotlin library providing managed AWS CodeArtifact client integration using the AWS SDK for Java, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-codeartifact-java-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `CodeArtifactClientBuildService` | `CodeartifactClient` | Synchronous CodeArtifact operations |
| `CodeArtifactAsyncClientBuildService` | `CodeartifactAsyncClient` | Asynchronous CodeArtifact operations |

Register a build service from a plugin or `build.gradle.kts`:

```kotlin
val codeartifact = gradle.sharedServices.registerIfAbsent("codeartifact", CodeArtifactClientBuildService::class) {
    parameters {
        regionId.set("us-east-1")
        defaultCredentials()
    }
}
```

Both parameters are optional. Leave `region` unset to fall back to the SDK's `DefaultAwsRegionProviderChain`,
and leave `credentials` unset to fall back to anonymous credentials.

## Value Sources

### **Deprecated.** `GetAuthorizationTokenValueSource`

Retrieves a temporary authorization token for a CodeArtifact domain:

> **Security note:** This Value Source is deprecated because its result (an authorization token) is cached by Gradle's configuration cache. See the class KDoc for details.

```kotlin
val token: Provider<String> = providers.of(GetAuthorizationTokenValueSource::class) {
    parameters {
        service.set(codeartifact)
        domain.set("my-domain")
        domainOwner.set("123456789012")
        duration.set(900L)
    }
}
```

Returns `null` if the call throws `CodeartifactException`.

### `GetRepositoryEndpointValueSource`

Returns the endpoint URL for a CodeArtifact repository (defaults to `EndpointType.IPV4` / `PackageFormat.GENERIC`):

```kotlin
val endpoint: Provider<String> = providers.of(GetRepositoryEndpointValueSource::class) {
    parameters {
        service.set(codeartifact)
        domain.set("my-domain")
        domainOwner.set("123456789012")
        repository.set("my-repo")
    }
}
```

Returns `null` if the call throws `CodeartifactException`.

### `ListPackageVersionsValueSource`

Returns all version strings for a CodeArtifact package, paginating internally:

```kotlin
val versions: Provider<List<String>> = providers.of(ListPackageVersionsValueSource::class) {
    parameters {
        service.set(codeartifact)
        domain.set("my-domain")
        domainOwner.set("123456789012")
        repository.set("my-repo")
        format.set(PackageFormat.GENERIC)
        namespace.set("my-namespace")
        packageValue.set("my-package")
    }
}
```

Returns `null` if the call throws `CodeartifactException`.

### `AbstractGetGenericAssetValueSource`

Extend to read a single asset from a CodeArtifact generic repository and transform the response:

```kotlin
abstract class MyAssetValueSource :
    AbstractGetGenericAssetValueSource<String, AbstractGetGenericAssetValueSource.Parameters>() {
    override fun doObtain(response: GetPackageVersionAssetResponse, input: AbortableInputStream): String =
        input.bufferedReader().readText()
}
```

Parameters: `service`, `domain`, `domainOwner`, `repository`, `namespace`, `packageValue`, `packageVersion`, `asset`.

## WorkActions

### `GetGenericPackageVersionAssetAction`

Downloads a single CodeArtifact generic-repo asset to a file:

```kotlin
workerExecutor.noIsolation().submit(GetGenericPackageVersionAssetAction::class) {
    service.set(codeartifact)
    domain.set("my-domain")
    domainOwner.set("123456789012")
    repository.set("my-repo")
    namespace.set("my-namespace")
    packageValue.set("my-package")
    packageVersion.set("1.0.0")
    asset.set("my-asset.zip")
    outputFile.set(layout.buildDirectory.file("downloads/my-asset.zip"))
}
```

### `PublishPackageVersionAction`

Uploads a file as a CodeArtifact generic package version asset:

```kotlin
workerExecutor.noIsolation().submit(PublishPackageVersionAction::class) {
    service.set(codeartifact)
    domain.set("my-domain")
    domainOwner.set("123456789012")
    repository.set("my-repo")
    namespace.set("my-namespace")
    packageValue.set("my-package")
    packageVersion.set("1.0.0")
    assetName.set("my-asset.jar")
    assetSHA256.set("…sha256 hex…")
    assetContent.set(layout.buildDirectory.file("dist/my-asset.jar"))
    unfinished.set(false)   // optional; set to true when uploading multiple assets to the same version
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-codeartifact-kotlin-base](../aws-codeartifact-kotlin-base) — Kotlin SDK variant
