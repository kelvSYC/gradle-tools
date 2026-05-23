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
    parameters {
        region.set("us-east-1")
        from(providers.credentials(AwsCredentials::class.java, "s3"))
    }
}
```

Both `region` and the credentials extension call are optional. Leave `region` unset to use the AWS SDK for Kotlin
default region provider chain. Omit the credentials call to skip the `credentialsProvider` assignment, in which
case the SDK applies its own default behavior. See [aws-kotlin-extensions](../aws-kotlin-extensions) for the full
set of credential configuration functions.

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

## Tasks: single-object operations

`DefaultTask` implementations for single-object S3 operations:

| Task | Purpose | Required properties |
|---|---|---|
| `DownloadFile` | Download one object to a local file | `service`, `bucket`, `key`, `outputFile` |
| `UploadFile` | Upload one local file | `service`, `bucket`, `key`, `inputFile` |
| `CopyObject` | Server-side copy between bucket/key pairs | `service`, `sourceBucket`, `sourceKey`, `destinationBucket`, `destinationKey` |
| `DeleteObject` | Delete one object | `service`, `bucket`, `key` |

```kotlin
tasks.register<UploadFile>("uploadResult") {
    service.set(s3)
    bucket.set("my-bucket")
    key.set("output/result.json")
    inputFile.set(layout.buildDirectory.file("result.json"))
}

tasks.register<CopyObject>("promoteArtifact") {
    service.set(s3)
    sourceBucket.set("staging-bucket")
    sourceKey.set("artifact-1.0.0.jar")
    destinationBucket.set("release-bucket")
    destinationKey.set("artifact-1.0.0.jar")
}
```

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
- [aws-s3-java-base](../aws-s3-java-base) — Java SDK variant with `S3TransferManager` and async client support
