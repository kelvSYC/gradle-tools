# Azure Blob Storage Base

A Kotlin library providing managed Azure Blob Storage client integration, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-blob-storage-base")
}
```

## Build Services

Four build services are available, covering account-level and container-scoped access in both synchronous and
asynchronous variants:

| Class | Scope | Client |
|---|---|---|
| `BlobServiceClientBuildService` | Storage account | `BlobServiceClient` |
| `BlobServiceAsyncClientBuildService` | Storage account | `BlobServiceAsyncClient` |
| `BlobContainerClientBuildService` | Single container | `BlobContainerClient` |
| `BlobContainerAsyncClientBuildService` | Single container | `BlobContainerAsyncClient` |

Each build service has its own `Params` interface providing `endpoint` and `credential`. The container-scoped
services add a `containerName` parameter.

### Account-level client

```kotlin
val blobService = gradle.sharedServices.registerIfAbsent("blob-service", BlobServiceClientBuildService::class) {
    parameters.endpoint.set("https://myaccount.blob.core.windows.net")
    parameters.credential.set(DefaultAzureCredentialBuilder().build())
}
```

### Container-scoped client

```kotlin
val container = gradle.sharedServices.registerIfAbsent("blob-container", BlobContainerClientBuildService::class) {
    parameters.endpoint.set("https://myaccount.blob.core.windows.net")
    parameters.containerName.set("my-container")
    parameters.credential.set(DefaultAzureCredentialBuilder().build())
}
```

### Parameter reference

| Parameter | Type | Description |
|---|---|---|
| `endpoint` | `Property<String>` | Azure Storage account endpoint URL, e.g. `https://{accountName}.blob.core.windows.net` |
| `credential` | `Property<TokenCredential>` | Azure credential. If absent, the client uses no authentication. Set to `DefaultAzureCredentialBuilder().build()` for the default credential chain. |
| `containerName` | `Property<String>` | (container-scoped services only) The name of the blob container |

## Task: `BatchDownloadFromAzureBlobStorage`

Downloads a collection of blobs concurrently via `WorkerExecutor.noIsolation()`. Set `service` to a registered
`BlobServiceClientBuildService`:

```kotlin
tasks.register<BatchDownloadFromAzureBlobStorage>("downloadBlobs") {
    service.set(blobService)

    registerArtifact("config") {
        containerName.set("my-container")
        blobName.set("config/settings.json")
        outputFile.set(layout.buildDirectory.file("config/settings.json"))
    }
}
```

Use `outputFiles` to wire outputs to downstream task inputs:

```kotlin
someTask.configure {
    inputFile.set(tasks.named<BatchDownloadFromAzureBlobStorage>("downloadBlobs").flatMap {
        it.outputFiles.getting("config").map { it.asFile }
    })
}
```

## Task: `BatchUploadToAzureBlobStorage`

Uploads a collection of artifacts to Azure Blob Storage, dispatching each upload via
`WorkerExecutor.noIsolation()`. Set `service` to a registered `BlobServiceClientBuildService`:

```kotlin
tasks.register<BatchUploadToAzureBlobStorage>("uploadBlobs") {
    service.set(blobService)

    registerArtifact("output") {
        containerName.set("my-container")
        blobName.set("output/result.json")
        inputFile.set(layout.buildDirectory.file("result.json"))
    }
}
```

Each artifact is uploaded independently via `UploadBlobAction`. The task fails if any upload throws.

## Work Actions

Individual blob operations for use with `WorkerExecutor.noIsolation()`. Each takes a
`Property<BlobServiceClientBuildService> service`:

| Action | Description |
|---|---|
| `DownloadBlobAction` | Downloads a single blob to a local file |
| `UploadBlobAction` | Uploads a single local file as a blob |
| `DeleteBlobAction` | Deletes a single blob |
| `CopyBlobAction` | Server-side copy within the same storage account |

## Value Source: `AbstractBlobStorageValueSource`

Extend `AbstractBlobStorageValueSource` to read a blob into memory and transform it:

```kotlin
abstract class MyBlobValueSource :
    AbstractBlobStorageValueSource<String, AbstractBlobStorageValueSource.Parameters>() {
    override fun doObtain(content: BinaryData): String = content.toString()
}
```

Use it in task configuration:

```kotlin
tasks.register("readFromAzure") {
    val content: Provider<String> = providers.of(MyBlobValueSource::class) {
        parameters {
            service.set(blobService)
            containerName.set("my-container")
            blobName.set("path/to/object.txt")
        }
    }

    doLast {
        println(content.get())
    }
}
```

The entire blob is read into memory as a `BinaryData`. Only use this for blobs that fit comfortably in memory.

## Value Source: `AbstractListBlobsValueSource`

Extend `AbstractListBlobsValueSource` to list blobs in a container and transform the listing:

```kotlin
abstract class MyListBlobsValueSource :
    AbstractListBlobsValueSource<List<String>, AbstractListBlobsValueSource.Parameters>() {
    override fun doObtain(blobs: List<BlobItem>): List<String> = blobs.map { it.name }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<BlobServiceClientBuildService>` | The build service supplying the account-scoped Blob Service client |
| `containerName` | `Property<String>` | The blob container name |
| `prefix` | `Property<String>` | Optional prefix filter |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Azure Storage Blob SDK for Java](https://learn.microsoft.com/en-us/java/api/overview/azure/storage-blob-readme)
