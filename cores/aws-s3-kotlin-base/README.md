# AWS S3 Kotlin Base

A Kotlin library providing managed AWS S3 client integration using the AWS SDK for Kotlin, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:aws-s3-kotlin-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `S3ClientBuildService` | `S3Client` (AWS SDK for Kotlin) |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val s3 = gradle.sharedServices.registerIfAbsent("s3", S3ClientBuildService::class) {
    parameters.region.set("us-east-1")
    parameters.credentials.set(providers.credentials(AwsCredentials::class.java, "s3").asCredentialsProvider)
}
```

Both parameters are optional. Leave `region` unset to fall back to the AWS SDK for Kotlin default region provider
chain, and leave `credentials` unset to fall back to the default credentials provider chain.

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
            service.set(s3)
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
| `service` | `Property<S3ClientBuildService>` | The shared build service |
| `bucket` | `Property<String>` | S3 bucket name |
| `key` | `Property<String>` | S3 object key |

## Tasks: `BatchDownloadFromS3` / `BatchUploadToS3`

These tasks perform concurrent batch downloads/uploads using Kotlin flows. The `client` is automatically wired
from the build service set on the task:

```kotlin
tasks.register<BatchDownloadFromS3>("downloadArtifacts") {
    service.set(s3)

    registerArtifact("config") { artifact ->
        artifact.bucket.set("my-bucket")
        artifact.key.set("config/settings.json")
        artifact.outputFile.set(layout.buildDirectory.file("config/settings.json"))
    }
}

tasks.register<BatchUploadToS3>("uploadResults") {
    service.set(s3)
    checksumAlgorithm.set(ChecksumAlgorithm.Sha256)  // optional
    retries.set(3)                                    // optional, defaults to 1

    registerArtifact("result") { artifact ->
        artifact.bucket.set("my-bucket")
        artifact.key.set("results/output.json")
        artifact.inputFile.set(layout.buildDirectory.file("output.json"))
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

To use an `S3Client` from outside `S3ClientBuildService`, use `AbstractBatchDownloadFromS3` /
`AbstractBatchUploadToS3` directly and set `client` manually.

## Value Source: `AbstractListObjectsValueSource`

Extend `AbstractListObjectsValueSource` to list keys under a bucket (optionally filtered by prefix) and transform
the results. Pagination is handled internally via the SDK Kotlin paginated flow — `doObtain` receives every
object summary in a single list.

```kotlin
abstract class AllKeysValueSource :
    AbstractListObjectsValueSource<List<String>, AbstractListObjectsValueSource.Parameters>() {
    override fun doObtain(objects: List<Object>): List<String> = objects.mapNotNull { it.key }
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
| `service` | `Property<S3ClientBuildService>` | The shared build service |
| `bucket` | `Property<String>` | S3 bucket name |
| `prefix` | `Property<String>` | Optional key prefix filter |

## WorkActions: single-object operations

Low-level `WorkAction` implementations for single-object S3 operations. Submit via `WorkerExecutor`:

| Action | Purpose | Required parameters |
|---|---|---|
| `DownloadFileAction` | Download one object to a local file | `bucket`, `key`, `outputFile` |
| `UploadFileAction` | Upload one local file | `bucket`, `key`, `inputFile` |
| `CopyObjectAction` | Server-side copy between bucket/key pairs | `sourceBucket`, `sourceKey`, `destinationBucket`, `destinationKey` |
| `DeleteObjectAction` | Delete one object | `bucket`, `key` |

All four also require `service` (a `Property<S3ClientBuildService>`).

```kotlin
workerExecutor.noIsolation().submit(UploadFileAction::class) {
    service.set(s3)
    bucket.set("my-bucket")
    key.set("output/result.json")
    inputFile.set(layout.buildDirectory.file("result.json"))
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
- [aws-s3-java-base](../aws-s3-java-base) — Java SDK variant with `S3TransferManager` and async client support
