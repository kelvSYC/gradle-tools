# Google Cloud Storage Base

A Kotlin library providing managed Google Cloud Storage client integration, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:google-cloud-storage-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `StorageClientBuildService` | `Storage` |

```kotlin
val gcs = gradle.sharedServices.registerIfAbsent("gcs", StorageClientBuildService::class) {
    parameters {
        projectId.set("my-gcp-project")
        applicationDefault()
        // serviceAccount(layout.projectDirectory.file("service-account.json"))
        // noCredentials()
    }
}
```

The parameter shape is provided by `GcpBuildServiceParams` from
[google-cloud-extensions](../google-cloud-extensions); use the extension functions on
`GcpBuildServiceParams` rather than setting fields directly. See that module's README for the full
list of supported credential sources.

| Parameter | Type | Description |
|---|---|---|
| `projectId` | `Property<String>` | GCP project ID. Leave unset to delegate to the SDK's default project resolution. |
| `credentialSource` | `Property<GcpCredentialSource>` | Which credentials object to construct. Set via the extension functions. Leave unset to delegate to the SDK's default credential resolution. |
| `credentialsFile` | `RegularFileProperty` | Service account JSON key file. Used when `credentialSource` is `SERVICE_ACCOUNT_JSON_FILE`. |
| `credentialsJson` | `Property<String>` | Inline service account JSON. Used when `credentialSource` is `SERVICE_ACCOUNT_JSON_INLINE`. |
| `accessToken` | `Property<String>` | Static OAuth2 access token. Used when `credentialSource` is `ACCESS_TOKEN`. |

### Credentials helpers

`GoogleCredentialsExtensions` provides convenience extensions for materialising service account
credentials from a file. These are no longer required for configuring `StorageClientBuildService`
(use `parameters.serviceAccount(file)` instead) but remain useful for other consumers:

```kotlin
// From a RegularFile directly
val creds: ServiceAccountCredentials = layout.projectDirectory.file("sa.json").asServiceAccountCredentials

// From a Provider<RegularFile>
val credsProvider: Provider<ServiceAccountCredentials> =
    layout.projectDirectory.file("sa.json").let { providers.provider { it } }.asServiceAccountCredentials
```

## Task: `BatchDownloadFromGCS`

Downloads a collection of artifacts from GCS in a single batched request:

```kotlin
tasks.register<BatchDownloadFromGCS>("downloadArtifacts") {
    service.set(gcs)

    registerArtifact("config") {
        bucket.set("my-bucket")
        blobName.set("config/settings.json")
        outputFile.set(layout.buildDirectory.file("config/settings.json"))
    }
}
```

Use `outputFiles` to wire outputs to downstream task inputs:

```kotlin
someTask.configure {
    inputFile.set(tasks.named<BatchDownloadFromGCS>("downloadArtifacts").flatMap {
        it.outputFiles.getting("config").map { it.asFile }
    })
}
```

The task fails if any artifact cannot be downloaded. All requests are submitted to GCS in a single batch call.

To use a `Storage` client from outside `StorageClientBuildService`, use `AbstractBatchDownloadFromGCS` directly
and set `client` manually.

## Task: `BatchUploadToGCS`

Uploads a collection of artifacts to GCS, dispatching each upload via `WorkerExecutor.noIsolation()`:

```kotlin
tasks.register<BatchUploadToGCS>("uploadArtifacts") {
    service.set(gcs)

    registerArtifact("output") {
        bucket.set("my-bucket")
        blobName.set("output/result.json")
        inputFile.set(layout.buildDirectory.file("result.json"))
    }
}
```

Each artifact is uploaded independently via `UploadFileAction`. The task fails if any upload throws.

`AbstractBatchUploadToGCS` is the same task minus the `@get:ServiceReference` annotation; subclass it if you
need to wire `service` differently (e.g. from a non-default registration).

## Value Source: `AbstractGCSValueSource`

Extend `AbstractGCSValueSource` to read a GCS blob into memory and transform it:

```kotlin
abstract class MyGCSValueSource : AbstractGCSValueSource<String, AbstractGCSValueSource.Parameters>() {
    override fun doObtain(content: ByteArray): String = content.decodeToString()
}
```

```kotlin
val content: Provider<String> = providers.of(MyGCSValueSource::class) {
    parameters {
        service.set(gcs)
        bucket.set("my-bucket")
        blobName.set("path/to/object.txt")
    }
}
```

The entire blob is read into memory as a `ByteArray`. Only use this for blobs that fit comfortably in memory.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<StorageClientBuildService>` | The shared build service |
| `bucket` | `Property<String>` | GCS bucket name |
| `blobName` | `Property<String>` | Blob name within the bucket |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Google Cloud Storage Java Client](https://cloud.google.com/java/docs/reference/google-cloud-storage/latest/overview)
