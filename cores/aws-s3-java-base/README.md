# AWS S3 Java Base

A Gradle plugin providing managed AWS S3 client integration using the AWS SDK for Java.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-s3-java-base")
}
```

## Client Types

Three client info types are registered:

| Client info type | Client type | Use case |
|---|---|---|
| `S3ClientInfo` | `S3Client` | Synchronous S3 operations |
| `S3AsyncClientInfo` | `S3AsyncClient` | Asynchronous S3 operations |
| `S3TransferManagerClientInfo` | `S3TransferManager` | High-throughput multipart transfers |

All three extend `AwsClientInfo` (except `S3TransferManagerClientInfo`). Use the convenience extensions on
`ClientsBaseExtension` to register clients:

```kotlin
serviceClients.registerAwsS3JavaClient("myS3Client") {
    region.set(Region.US_EAST_1)
    credentials.set(DefaultCredentialsProvider.create())
}

serviceClients.registerAwsS3AsyncJavaClient("myS3AsyncClient") {
    region.set(Region.US_EAST_1)
    credentials.set(DefaultCredentialsProvider.create())
}
```

When registering an `S3TransferManagerClientInfo`, pass the underlying `S3AsyncClient` as the `baseClient` so both
clients share a lifecycle through `ClientsBaseService`:

```kotlin
// Register the async client first
serviceClients.registerAwsS3AsyncJavaClient("myS3AsyncClient") {
    region.set(Region.US_EAST_1)
    credentials.set(DefaultCredentialsProvider.create())
}

// Wire it as the base client for the transfer manager
serviceClients.registerAwsS3TransferManagerJavaClient("myTransferManager") {
    baseClient.set(serviceClients.getClient<S3AsyncClient, S3AsyncClientInfo>("myS3AsyncClient").get())
}
```

### `AwsClientInfo` properties

| Property | Type | Description |
|---|---|---|
| `region` | `Property<Region>` | AWS region. Leave unset to use `DefaultAwsRegionProviderChain`. |
| `credentials` | `Property<AwsCredentialsProvider>` | Credentials. If absent, uses `AnonymousCredentialsProvider`. |

### `S3TransferManagerClientInfo` properties

| Property | Type | Description |
|---|---|---|
| `baseClient` | `Property<S3AsyncClient>` | The underlying async client powering the transfer manager |
| `uploadDirectoryFollowSymbolicLinks` | `Property<Boolean>` | Whether to follow symlinks during directory uploads. Defaults to `false`. |

## Value Source: `AbstractS3ValueSource`

Extend `AbstractS3ValueSource` to read an S3 object into memory and transform it:

```kotlin
abstract class MyS3ValueSource : AbstractS3ValueSource<String, AbstractS3ValueSource.Parameters>() {
    override fun doObtain(content: ResponseBytes<GetObjectResponse>): String =
        content.asUtf8String()
}
```

Use it in task configuration:

```kotlin
tasks.register("readFromS3") {
    val content: Provider<String> = providers.of(MyS3ValueSource::class) {
        parameters {
            service.set(serviceClients.service)
            clientName.set("myS3Client")
            bucket.set("my-bucket")
            key.set("path/to/object.txt")
        }
    }

    doLast {
        println(content.get())
    }
}
```

The entire object is read into memory as a `ResponseBytes<GetObjectResponse>`. Only use this for objects that fit
comfortably in memory.

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service (set from `serviceClients.service`) |
| `clientName` | `Property<String>` | Registered name of a `S3ClientInfo` |
| `bucket` | `Property<String>` | S3 bucket name |
| `key` | `Property<String>` | S3 object key |

## Tasks: `BatchDownloadFromS3` / `BatchUploadToS3`

These tasks perform concurrent batch downloads/uploads using an `S3TransferManager` client. Register artifacts using
`registerArtifact`, then set `clientName` to a registered `S3TransferManagerClientInfo`. The plugin auto-wires
`client` from `clientsService` and `clientName`.

```kotlin
tasks.register<BatchDownloadFromS3>("downloadArtifacts") {
    clientName.set("myTransferManager")

    registerArtifact("config") {
        bucket.set("my-bucket")
        key.set("config/settings.json")
        outputFile.set(layout.buildDirectory.file("config/settings.json"))
    }
    registerArtifact("data") {
        bucket.set("my-bucket")
        key.set("data/input.csv")
        outputFile.set(layout.buildDirectory.file("data/input.csv"))
    }
}
```

Use `outputFiles` to wire the outputs of `BatchDownloadFromS3` to inputs of other tasks:

```kotlin
tasks.register<MyProcessingTask>("process") {
    inputFile.set(tasks.named<BatchDownloadFromS3>("downloadArtifacts").flatMap {
        it.outputFiles.getting("config").map { it.asFile }
    })
}
```

`BatchUploadToS3` works the same way, with `inputFile` in place of `outputFile` per artifact.

To use a client from outside `ClientsBaseService`, use `AbstractBatchDownloadFromS3` / `AbstractBatchUploadToS3`
directly and set `client` manually.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-java-extensions](../aws-java-extensions) — `AwsClientInfo` base interface and Gradle credentials adapters