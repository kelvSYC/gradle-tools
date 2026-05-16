# AWS ECR Java Base

A Kotlin library providing managed AWS Elastic Container Registry (ECR) client integration using the AWS SDK for
Java, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-ecr-java-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `EcrClientBuildService` | `EcrClient` (AWS SDK for Java) |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val ecr = gradle.sharedServices.registerIfAbsent("ecr", EcrClientBuildService::class) {
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

Returns the base64-encoded `user:password` ECR authorization token for the caller's default registry.

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

**Task-execution use cases** (e.g. running `docker login` as a build step): use the `EcrClientBuildService` client directly inside a `WorkAction.execute()` body instead. The `ValueSource` abstraction adds no value there.

### `DescribeRepositoriesValueSource`

Returns a `Map<String, String>` keyed by repository name with the corresponding repository URI as the value:

```kotlin
val repos: Provider<Map<String, String>> = providers.of(DescribeRepositoriesValueSource::class) {
    parameters {
        service.set(ecr)
    }
}
```

## WorkAction: `BatchDeleteImageAction`

Deletes a set of images by tag from an ECR repository:

```kotlin
workerExecutor.noIsolation().submit(BatchDeleteImageAction::class) {
    service.set(ecr)
    repositoryName.set("my-repo")
    imageTags.set(setOf("v1.0", "v1.1"))
}
```

To delete by digest instead of tag, use the underlying SDK directly.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-ecr-kotlin-base](../aws-ecr-kotlin-base) — Kotlin SDK variant
