# AWS S3 Kotlin Base

A Gradle plugin providing managed AWS S3 client integration using the AWS SDK for Kotlin.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.aws-s3-kotlin-base")
}
```

## Client Type

One client info type is registered:

| Client info type | Client type |
|---|---|
| `S3ClientInfo` | `S3Client` (AWS SDK for Kotlin) |

`S3ClientInfo` extends `AwsClientInfo` from `aws-kotlin-extensions`. Register a client:

```kotlin
serviceClients.registerAwsS3KotlinClient("myS3Client") {
    region.set("us-east-1")
    credentials.set(providers.credentials(AwsCredentials::class.java, "myS3Client").asCredentialsProvider)
}
```

## Value Source: `AbstractS3ValueSource`

Extend `AbstractS3ValueSource` to read an S3 object and transform the response:

```kotlin
abstract class MyS3ValueSource : AbstractS3ValueSource<String, AbstractS3ValueSource.Parameters>() {
    override fun doObtain(response: GetObjectResponse): String? =
        response.body?.decodeToString()
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

    doLast { println(content.get()) }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of an `S3ClientInfo` |
| `bucket` | `Property<String>` | S3 bucket name |
| `key` | `Property<String>` | S3 object key |

## Tasks: `BatchDownloadFromS3` / `BatchUploadToS3`

These tasks perform concurrent batch downloads/uploads using Kotlin flows. The plugin auto-wires `client` from
`service` and `clientName`. Set `clientName` to a registered `S3ClientInfo`:

```kotlin
tasks.register<BatchDownloadFromS3>("downloadArtifacts") {
    clientName.set("myS3Client")

    registerArtifact("config") {
        bucket.set("my-bucket")
        key.set("config/settings.json")
        outputFile.set(layout.buildDirectory.file("config/settings.json"))
    }
}

tasks.register<BatchUploadToS3>("uploadResults") {
    clientName.set("myS3Client")
    checksumAlgorithm.set(ChecksumAlgorithm.Sha256)  // optional
    retries.set(3)                                    // optional, defaults to 1

    registerArtifact("result") {
        bucket.set("my-bucket")
        key.set("results/output.json")
        inputFile.set(layout.buildDirectory.file("output.json"))
    }
}
```

Use `outputFileForArtifact(name)` on `BatchDownloadFromS3` to wire outputs to downstream task inputs:

```kotlin
someTask.configure {
    inputFile.set(tasks.named<BatchDownloadFromS3>("downloadArtifacts")
        .flatMap { it.outputFileForArtifact("config") })
}
```

Both tasks fail if any artifact operation fails after exhausting retries. Non-retriable errors
(`ClientException`) are never retried.

To use an `S3Client` from outside `ClientsBaseService`, use `AbstractBatchDownloadFromS3` /
`AbstractBatchUploadToS3` directly and set `client` manually.

## WorkActions: `UploadFileAction` / `DownloadFileAction`

Low-level `WorkAction` implementations for single-file S3 operations. Submit via `WorkerExecutor`:

```kotlin
workerExecutor.noIsolation().submit(UploadFileAction::class) {
    service.set(serviceClients.service)
    clientName.set("myS3Client")
    bucket.set("my-bucket")
    key.set("output/result.json")
    inputFile.set(layout.buildDirectory.file("result.json"))
}

workerExecutor.noIsolation().submit(DownloadFileAction::class) {
    service.set(serviceClients.service)
    clientName.set("myS3Client")
    bucket.set("my-bucket")
    key.set("config/settings.json")
    outputFile.set(layout.buildDirectory.file("settings.json"))
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-kotlin-extensions](../aws-kotlin-extensions) — `AwsClientInfo` base interface and credential adapters
- [aws-s3-java-base](../aws-s3-java-base) — Java SDK variant with `S3TransferManager` and async client support
