# AWS S3 Java Base

A Kotlin library providing managed AWS S3 client integration using the AWS SDK for Java, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-s3-java-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `S3ClientBuildService` | `S3Client` | Synchronous S3 operations |
| `S3AsyncClientBuildService` | `S3AsyncClient` (CRT) | Asynchronous S3 operations; required input for `S3TransferManagerBuildService` |
| `S3TransferManagerBuildService` | `S3TransferManager` | High-throughput multipart transfers |

### Direct clients

```kotlin
val s3 = gradle.sharedServices.registerIfAbsent("s3", S3ClientBuildService::class) {
    parameters {
        regionId.set("us-east-1")
        defaultCredentials()
    }
}

val s3Async = gradle.sharedServices.registerIfAbsent("s3-async", S3AsyncClientBuildService::class) {
    parameters {
        regionId.set("us-east-1")
        defaultCredentials()
    }
}
```

Leave `region` unset to fall back to `DefaultAwsRegionProviderChain`. Leave `credentials` unset to fall back to
`AnonymousCredentialsProvider`.

### Transfer manager (wraps an `S3AsyncClient`)

```kotlin
val s3TransferManager = gradle.sharedServices.registerIfAbsent("s3-tm", S3TransferManagerBuildService::class) {
    parameters.baseService.set(s3Async)
    parameters.uploadDirectoryFollowSymbolicLinks.set(false) // optional, defaults to false
}
```

`baseService` must be set to a registered `S3AsyncClientBuildService`. The wrapped client is resolved lazily —
the underlying async service is not instantiated until the transfer manager itself is first accessed. The
underlying `S3AsyncClient` is not closed by the transfer manager; the `S3AsyncClientBuildService` owns its
lifecycle.

| Parameter | Type | Description |
|---|---|---|
| `baseService` | `Property<S3AsyncClientBuildService>` | The build service supplying the underlying async client |
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
            service.set(s3)
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
| `service` | `Property<S3ClientBuildService>` | Build service supplying the synchronous S3 client |
| `bucket` | `Property<String>` | S3 bucket name |
| `key` | `Property<String>` | S3 object key |

## Tasks: `BatchDownloadFromS3` / `BatchUploadToS3`

These tasks perform concurrent batch downloads/uploads using an `S3TransferManager`. Register artifacts using
`registerArtifact`, then set `service` to a registered `S3TransferManagerBuildService`.

```kotlin
tasks.register<BatchDownloadFromS3>("downloadArtifacts") {
    service.set(s3TransferManager)

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

To use a client from outside the build-service infrastructure, use `AbstractBatchDownloadFromS3` /
`AbstractBatchUploadToS3` directly and set `client` manually.

## Value Source: `AbstractListObjectsValueSource`

Extend `AbstractListObjectsValueSource` to list keys under a bucket (optionally filtered by prefix) and transform
the results. Pagination is handled internally — `doObtain` receives every `S3Object` summary in a single list.

```kotlin
abstract class AllKeysValueSource :
    AbstractListObjectsValueSource<List<String>, AbstractListObjectsValueSource.Parameters>() {
    override fun doObtain(objects: List<S3Object>): List<String> = objects.map { it.key() }
}

val keys: Provider<List<String>> = providers.of(AllKeysValueSource::class) {
    parameters {
        service.set(s3)
        bucket.set("my-bucket")
        prefix.set("artifacts/")  // optional
    }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<S3ClientBuildService>` | Build service supplying the synchronous S3 client |
| `bucket` | `Property<String>` | S3 bucket name |
| `prefix` | `Property<String>` | Optional key prefix filter |

## WorkActions: single-object operations

Low-level `WorkAction` implementations using the synchronous `S3Client`. Submitting through
`WorkerExecutor.noIsolation()` gives Gradle-managed parallel execution without requiring an
`S3TransferManager` registration.

| Action | Purpose | Required parameters |
|---|---|---|
| `DownloadFileAction` | Download one object to a local file | `bucket`, `key`, `outputFile` |
| `UploadFileAction` | Upload one local file | `bucket`, `key`, `inputFile` |
| `CopyObjectAction` | Server-side copy between bucket/key pairs | `sourceBucket`, `sourceKey`, `destinationBucket`, `destinationKey` |
| `DeleteObjectAction` | Delete one object | `bucket`, `key` |

All four also require `service` referencing a registered `S3ClientBuildService`.

```kotlin
workerExecutor.noIsolation().submit(DownloadFileAction::class) {
    service.set(s3)
    bucket.set("my-bucket")
    key.set("config/settings.json")
    outputFile.set(layout.buildDirectory.file("settings.json"))
}

workerExecutor.noIsolation().submit(CopyObjectAction::class) {
    service.set(s3)
    sourceBucket.set("staging-bucket")
    sourceKey.set("artifact-1.0.0.jar")
    destinationBucket.set("release-bucket")
    destinationKey.set("artifact-1.0.0.jar")
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [aws-s3-kotlin-base](../aws-s3-kotlin-base) — Kotlin SDK variant
