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
    parameters {
        region.set("us-east-1")
        from(providers.credentials(AwsCredentials::class.java, "ca"))
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

## **Deprecated.** Value Source: `GetAuthorizationTokenValueSource`

Retrieves an AWS CodeArtifact authorization token.

> **Deprecated — configuration cache unsafe.** Gradle serializes `ValueSource.obtain()` results to `.gradle/configuration-cache/` in plaintext at cache-write time. The token value is written to disk. See the class KDoc for full safety constraints, including the `@get:Internal` and private `val` caveats.

**Repository authentication** is the common use case, but Gradle resolves `maven { credentials { } }` blocks at configuration time — any token obtained here will be stored in the cache regardless of how it was fetched. There is no deferred-credential mechanism. The pre-generation pattern avoids this ValueSource entirely:

1. Obtain the token before Gradle runs, in a CI startup step or pipeline script:
   ```bash
   export CODEARTIFACT_TOKEN=$(aws codeartifact get-authorization-token \
     --domain my-domain \
     --domain-owner 111122223333 \
     --duration-seconds 3600 \
     --query authorizationToken \
     --output text)
   ```

2. Reference it via `providers.environmentVariable()` in the credentials block. Gradle stores the env var _name_ in the config cache and re-reads the value on every build — the token itself is never cached:
   ```kotlin
   maven {
       url = uri("https://my-domain-111122223333.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo/")
       credentials {
           username = "aws"
           password = providers.environmentVariable("CODEARTIFACT_TOKEN").get()
       }
   }
   ```

CodeArtifact tokens are valid for up to 12 hours (configurable via `--duration-seconds`). The threat model of the pre-generation pattern is equivalent to the CI environment variable attack surface — no new exposure.

**Task-execution use cases**: use the `CodeArtifactClientBuildService` client directly inside a `WorkAction.execute()` body instead. The `ValueSource` abstraction adds no value there.

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

> **Security note:** Whatever `doObtain()` returns is serialized to `.gradle/configuration-cache/` in plaintext at cache-write time — this applies whether the resulting `Provider` is stored in a task `@Input`, a `@get:Internal` property, or a private `val`. If the asset contains sensitive data (private keys, credentials, tokens), use `GetGenericPackageVersionAssetAction` to download it to a file at task execution time, or call the `CodeArtifactClientBuildService` client directly inside a `WorkAction.execute()` body. Non-sensitive assets (version manifests, metadata) are safe to use at configuration time.

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
