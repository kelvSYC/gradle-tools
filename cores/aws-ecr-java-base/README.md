# AWS ECR Java Base

A Gradle plugin providing managed AWS Elastic Container Registry (ECR) client integration using the AWS SDK for
Java.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-ecr-java-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `EcrClientInfo` | `EcrClient` (AWS SDK for Java) |

`EcrClientInfo` extends `AwsClientInfo` from `aws-java-extensions`. Register a client:

```kotlin
serviceClients.service.get().registerIfAbsent<EcrClientInfo>("ecr") {
    region.set(Region.US_EAST_1)
    credentials.set(DefaultCredentialsProvider.create())
}
```

## Value Sources

### `GetAuthorizationTokenValueSource`

Retrieves the base64-encoded `user:password` authorization token for the caller's default ECR registry, suitable
for `docker login`:

```kotlin
val token: Provider<String> = providers.of(GetAuthorizationTokenValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("ecr")
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
        service.set(serviceClients.service)
        clientName.set("ecr")
    }
}
```

## WorkActions

### `BatchDeleteImageAction`

Deletes a set of images, by tag, from an ECR repository:

```kotlin
workerExecutor.noIsolation().submit(BatchDeleteImageAction::class) {
    service.set(serviceClients.service)
    clientName.set("ecr")
    repositoryName.set("my-repo")
    imageTags.addAll("v1.0", "v1.1-rc1")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of an `EcrClientInfo` |
| `repositoryName` | `Property<String>` | Repository to delete images from |
| `imageTags` | `SetProperty<String>` | Set of image tags to delete |

To delete by digest instead of tag, use the SDK directly.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-java-extensions](../aws-java-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-ecr-kotlin-base](../aws-ecr-kotlin-base) — Kotlin SDK variant
