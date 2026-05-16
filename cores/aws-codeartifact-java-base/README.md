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

Retrieves a temporary authorization token for a CodeArtifact domain.

> **Deprecated — configuration cache unsafe.** Gradle serializes `ValueSource.obtain()` results to `.gradle/configuration-cache/` in plaintext at cache-write time. The token value is written to disk. See the class KDoc for full safety constraints, including the `@get:Internal` and private `val` caveats.

**Repository authentication** is the common use case, but Gradle resolves `maven { credentials { } }` blocks at configuration time — any token obtained here will be stored in the cache regardless of how it was fetched. There is no deferred-credential mechanism. The pre-generation pattern avoids this ValueSource entirely:

1. Obtain the token before Gradle runs, in a CI startup step or pipeline script:
   ```bash
   export CODEARTIFACT_TOKEN=$(aws codeartifact get-authorization-token \
     --domain my-domain \
     --domain-owner 123456789012 \
     --duration-seconds 3600 \
     --query authorizationToken \
     --output text)
   ```

2. Reference it via `providers.environmentVariable()` in the credentials block. Gradle stores the env var _name_ in the config cache and re-reads the value on every build — the token itself is never cached:
   ```kotlin
   maven {
       url = uri("https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo/")
       credentials {
           username = "aws"
           password = providers.environmentVariable("CODEARTIFACT_TOKEN").get()
       }
   }
   ```

CodeArtifact tokens are valid for up to 12 hours (configurable via `--duration-seconds`). The threat model of the pre-generation pattern is equivalent to the CI environment variable attack surface — no new exposure.

**Task-execution use cases**: use the `CodeArtifactClientBuildService` client directly inside a `WorkAction.execute()` body instead. The `ValueSource` abstraction adds no value there. Returns `null` if the call throws `CodeartifactException`.

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

> **Security note:** Whatever `doObtain()` returns is serialized to `.gradle/configuration-cache/` in plaintext at cache-write time — this applies whether the resulting `Provider` is stored in a task `@Input`, a `@get:Internal` property, or a private `val`. If the asset contains sensitive data (private keys, credentials, tokens), use `GetGenericPackageVersionAssetAction` to download it to a file at task execution time, or call the `CodeArtifactClientBuildService` client directly inside a `WorkAction.execute()` body. Non-sensitive assets (version manifests, metadata) are safe to use at configuration time.

## WorkActions

### `AbstractGetAuthorizationTokenWorkAction`

Retrieves a CodeArtifact authorization token inside a `WorkAction`, keeping the token out of the
Gradle configuration cache. Subclass and implement `doExecute` to use the token:

```kotlin
abstract class PublishAction : AbstractGetAuthorizationTokenWorkAction() {
    override fun doExecute(token: String) {
        // use token for Maven/npm/pip repository authentication
    }
}

workerExecutor.noIsolation().submit(PublishAction::class) {
    service.set(codeartifact)
    domain.set("my-domain")
    domainOwner.set("123456789012")
    duration.set(3600L)
}
```

Token validity is configurable up to 43200 seconds (12 hours). There is no explicit revocation
API — `doExecute` provides the correct execution-time scope. The token must not escape `doExecute`:
storing it in a WorkParameters property (even `@get:Internal`), a task input, or a shared file
writes it to `.gradle/configuration-cache/` in plaintext.

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
