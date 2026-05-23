# AWS ECR Kotlin Base

A Kotlin library providing managed AWS Elastic Container Registry (ECR) client integration using the AWS SDK
for Kotlin, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-ecr-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `EcrClientBuildService` | `EcrClient` (AWS SDK for Kotlin) |

```kotlin
val ecr = gradle.sharedServices.registerIfAbsent("ecr", EcrClientBuildService::class) {
    parameters {
        region.set("us-east-1")
        defaultCredentials()
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

## Value Sources

### **Deprecated.** `GetAuthorizationTokenValueSource`

Returns the base64-encoded `user:password` authorization token for the caller's default ECR registry, suitable
for `docker login`. The first entry of the response's `authorizationData` list is returned.

> **Deprecated — configuration cache unsafe.** Gradle serializes `ValueSource.obtain()` results to `.gradle/configuration-cache/` in plaintext at cache-write time. The token value is written to disk. See the class KDoc for full safety constraints, including the `@get:Internal` and private `val` caveats.

**Repository authentication** is the common use case, but Gradle resolves `maven { credentials { } }` blocks at configuration time — any token obtained here will be stored in the cache regardless of how it was fetched. There is no deferred-credential mechanism. The pre-generation pattern avoids this ValueSource entirely:

1. Obtain the token before Gradle runs, in a CI startup step or pipeline script:
   ```bash
   export ECR_TOKEN=$(aws ecr get-authorization-token \
     --output text \
     --query 'authorizationData[0].authorizationToken')
   ```

2. Reference it via `providers.environmentVariable()` in the credentials block. Gradle stores the env var _name_ in the config cache and re-reads the value on every build — the token itself is never cached:
   ```kotlin
   maven {
       url = uri("https://<account>.dkr.ecr.<region>.amazonaws.com")
       credentials {
           username = "AWS"
           password = providers.environmentVariable("ECR_TOKEN").get()
       }
   }
   ```

ECR tokens are valid for up to 12 hours. The threat model of the pre-generation pattern is equivalent to the CI environment variable attack surface — no new exposure.

**Task-execution use cases** (e.g. running `docker login` as a build step): use the `EcrClientBuildService` client directly inside a `WorkAction.execute()` body instead. To target a non-default registry, use the SDK directly.

### `DescribeRepositoriesValueSource`

Lists all ECR repositories visible to the configured client, returning a `Map<String, String>` keyed by
repository name with the repository URI as the value. Pagination is handled internally:

```kotlin
val repositories: Provider<Map<String, String>> = providers.of(DescribeRepositoriesValueSource::class) {
    parameters {
        service.set(ecr)
    }
}
```

## Tasks

### `AbstractGetAuthorizationToken`

Retrieves an ECR authorization token inside a `@TaskAction`, keeping the token out of the Gradle
configuration cache. Subclass and implement `doExecute` to use the token:

```kotlin
abstract class DockerLogin : AbstractGetAuthorizationToken() {
    override fun doExecute(token: String) {
        // token is the base64-encoded "AWS:password" string suitable for docker login
    }
}

tasks.register<DockerLogin>("dockerLogin") {
    service.set(ecr)
}
```

ECR tokens are valid for up to 12 hours and have no explicit revocation API — `doExecute` provides
the correct execution-time scope. The token must not escape `doExecute`: storing it in a
task property or a shared file would write it to disk.

### `BatchDeleteImage`

Deletes a set of images, by tag, from an ECR repository:

```kotlin
tasks.register<BatchDeleteImage>("deleteOldImages") {
    service.set(ecr)
    repositoryName.set("my-repo")
    imageTags.addAll("v1.0", "v1.1-rc1")
}
```

| Property | Type | Description |
|---|---|---|
| `service` | `Property<EcrClientBuildService>` | Build service supplying the ECR client |
| `repositoryName` | `Property<String>` | Repository to delete images from |
| `imageTags` | `SetProperty<String>` | Set of image tags to delete |

To delete by digest instead of tag, use the SDK directly.

## Why no WorkActions

The AWS Kotlin SDK exposes all service calls as `suspend` functions. A `WorkAction` that wraps a single suspend call reduces to:

```kotlin
override fun execute() {
    runBlocking { singleSuspendCall() }
}
```

This adds ceremony with no benefit: no return values, no isolation beyond what coroutines already provide, and no concurrency advantage (Gradle's task graph handles cross-task concurrency; coroutines handle within-task concurrency). WorkActions were designed for blocking Java SDK calls to avoid tying up Gradle's worker thread pool — that problem doesn't exist with a coroutine-based SDK.

Accordingly, this component exposes `DefaultTask` subclasses instead. Plugin authors needing compound operations should compose via Gradle task dependencies (sequential) or call `service.get().getClient()` directly inside a `runBlocking { coroutineScope { } }` block (parallel).

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-ecr-java-base](../aws-ecr-java-base) — Java SDK variant
