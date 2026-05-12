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
    parameters.region.set("us-east-1")
    parameters.credentials.set(providers.credentials(AwsCredentials::class.java, "ecr").asCredentialsProvider)
}
```

## Value Sources

### `GetAuthorizationTokenValueSource`

Retrieves the base64-encoded `user:password` authorization token for the caller's default ECR registry, suitable
for `docker login`:

```kotlin
val token: Provider<String> = providers.of(GetAuthorizationTokenValueSource::class) {
    parameters {
        service.set(ecr)
    }
}
```

The first entry of the response's `authorizationData` list is returned. To target a non-default registry, use
the SDK directly.

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

## WorkActions

### `BatchDeleteImageAction`

Deletes a set of images, by tag, from an ECR repository:

```kotlin
workerExecutor.noIsolation().submit(BatchDeleteImageAction::class) {
    service.set(ecr)
    repositoryName.set("my-repo")
    imageTags.addAll("v1.0", "v1.1-rc1")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<EcrClientBuildService>` | Build service supplying the ECR client |
| `repositoryName` | `Property<String>` | Repository to delete images from |
| `imageTags` | `SetProperty<String>` | Set of image tags to delete |

To delete by digest instead of tag, use the SDK directly.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-ecr-java-base](../aws-ecr-java-base) — Java SDK variant
