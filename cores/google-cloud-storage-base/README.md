# Google Cloud Storage Base

A Gradle plugin providing managed Google Cloud Storage client integration.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.google-cloud-storage-base")
}
```

## Client Registration

The client info type is `StorageClientInfo`. Use the `registerGoogleCloudServiceClient` convenience extension:

```kotlin
val credentialsFile = layout.projectDirectory.file("service-account.json")

serviceClients.registerGoogleCloudServiceClient("myGcsClient") {
    projectId.set("my-gcp-project")
    credentials.set(credentialsFile.asServiceAccountCredentials)
}
```

### `StorageClientInfo` properties

| Property | Type | Description |
|---|---|---|
| `projectId` | `Property<String>` | The GCP project ID |
| `credentials` | `Property<Credentials>` | GCP credentials. If absent, the client uses no authentication. Set to `GoogleCredentials.getApplicationDefault()` for ADC. |

### Credentials helpers

`GoogleCredentialsExtensions` provides convenience extensions for loading service account credentials from a file:

```kotlin
// From a RegularFile directly
val creds: ServiceAccountCredentials = layout.projectDirectory.file("sa.json").asServiceAccountCredentials

// From a Provider<RegularFile>
val credsProvider: Provider<ServiceAccountCredentials> =
    layout.projectDirectory.file("sa.json").let { providers.provider { it } }.asServiceAccountCredentials
```

## Task: `BatchDownloadFromGCS`

Downloads a collection of artifacts from GCS in a single batched request. Set `clientName` to a registered
`StorageClientInfo`; the plugin auto-wires the underlying `Storage` client:

```kotlin
tasks.register<BatchDownloadFromGCS>("downloadArtifacts") {
    clientName.set("myGcsClient")

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

To use a `Storage` client from outside `ClientsBaseService`, use `AbstractBatchDownloadFromGCS` directly and set
`client` manually.

## Task: `BatchUploadToGCS`

Uploads a collection of artifacts to GCS, dispatching each upload via `WorkerExecutor.noIsolation()`. Set `clientName`
to a registered `StorageClientInfo`:

```kotlin
tasks.register<BatchUploadToGCS>("uploadArtifacts") {
    clientName.set("myGcsClient")

    registerArtifact("output") {
        bucket.set("my-bucket")
        blobName.set("output/result.json")
        inputFile.set(layout.buildDirectory.file("result.json"))
    }
}
```

Each artifact is uploaded independently via `UploadFileAction`. The task fails if any upload throws.

To use a client from outside `ClientsBaseService`, use `AbstractBatchUploadToGCS` directly and set `service`/`clientName`
on the abstract class.

## Value Source: `AbstractGCSValueSource`

Extend `AbstractGCSValueSource` to read a GCS blob into memory and transform it:

```kotlin
abstract class MyGCSValueSource : AbstractGCSValueSource<String, AbstractGCSValueSource.Parameters>() {
    override fun doObtain(content: ByteArray): String = content.decodeToString()
}
```

Use it in task configuration:

```kotlin
tasks.register("readFromGCS") {
    val content: Provider<String> = providers.of(MyGCSValueSource::class) {
        parameters {
            service.set(serviceClients.service)
            clientName.set("myGcsClient")
            bucket.set("my-bucket")
            blobName.set("path/to/object.txt")
        }
    }

    doLast {
        println(content.get())
    }
}
```

The entire blob is read into memory as a `ByteArray`. Only use this for blobs that fit comfortably in memory.

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service (set from `serviceClients.service`) |
| `clientName` | `Property<String>` | Registered name of a `StorageClientInfo` |
| `bucket` | `Property<String>` | GCS bucket name |
| `blobName` | `Property<String>` | Blob name within the bucket |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Google Cloud Storage Java Client](https://cloud.google.com/java/docs/reference/google-cloud-storage/latest/overview)
