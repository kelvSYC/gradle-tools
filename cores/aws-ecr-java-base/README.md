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
    parameters.region.set(Region.US_EAST_1)
    parameters.credentials.set(DefaultCredentialsProvider.create())
}
```

Both parameters are optional. Leave `region` unset to fall back to the SDK's `DefaultAwsRegionProviderChain`,
and leave `credentials` unset to fall back to anonymous credentials.

## Value Sources

### `GetAuthorizationTokenValueSource`

Returns the base64-encoded `user:password` ECR authorization token for the caller's default registry:

```kotlin
val token: Provider<String> = providers.of(GetAuthorizationTokenValueSource::class) {
    parameters {
        service.set(ecr)
    }
}
```

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
