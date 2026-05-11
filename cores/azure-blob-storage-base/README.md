# Azure Blob Storage Base

A Gradle plugin providing managed Azure Blob Storage client integration.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.azure-blob-storage-base")
}
```

## Client Registration

Four client info types are available, covering account-level and container-scoped access in both synchronous and
asynchronous variants:

| Type | Scope | Client |
|---|---|---|
| `BlobServiceClientInfo` | Storage account | `BlobServiceClient` |
| `BlobServiceAsyncClientInfo` | Storage account | `BlobServiceAsyncClient` |
| `BlobContainerClientInfo` | Single container | `BlobContainerClient` |
| `BlobContainerAsyncClientInfo` | Single container | `BlobContainerAsyncClient` |

All four share a base interface `AzureBlobStorageClientInfo` providing `endpoint` and `credential` properties.
Container-scoped types add a `containerName` property.

### Account-level client

Use `registerAzureBlobServiceClient` for operations spanning multiple containers:

```kotlin
serviceClients.registerAzureBlobServiceClient("myAzureStorage") {
    endpoint.set("https://myaccount.blob.core.windows.net")
    credential.set(DefaultAzureCredentialBuilder().build())
}
```

### Container-scoped client

Use `registerAzureBlobContainerClient` when all operations target a single container:

```kotlin
serviceClients.registerAzureBlobContainerClient("myContainer") {
    endpoint.set("https://myaccount.blob.core.windows.net")
    containerName.set("my-container")
    credential.set(DefaultAzureCredentialBuilder().build())
}
```

### `AzureBlobStorageClientInfo` properties

| Property | Type | Description |
|---|---|---|
| `endpoint` | `Property<String>` | Azure Storage account endpoint URL |
| `credential` | `Property<TokenCredential>` | Azure credential. If absent, the client uses no authentication. Set to `DefaultAzureCredentialBuilder().build()` for the default credential chain. |

Container-scoped types add:

| Property | Type | Description |
|---|---|---|
| `containerName` | `Property<String>` | The name of the blob container |

## Task: `BatchDownloadFromAzureBlobStorage`

Downloads a collection of blobs concurrently via `WorkerExecutor.noIsolation()`. Set `clientName` to a registered
`BlobServiceClientInfo`:

```kotlin
tasks.register<BatchDownloadFromAzureBlobStorage>("downloadBlobs") {
    clientName.set("myAzureStorage")

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

To use a `BlobServiceClient` from outside `ClientsBaseService`, use `AbstractBatchDownloadFromAzureBlobStorage`
directly and set `service`/`clientName` manually.

## Task: `BatchUploadToAzureBlobStorage`

Uploads a collection of artifacts to Azure Blob Storage, dispatching each upload via
`WorkerExecutor.noIsolation()`. Set `clientName` to a registered `BlobServiceClientInfo`:

```kotlin
tasks.register<BatchUploadToAzureBlobStorage>("uploadBlobs") {
    clientName.set("myAzureStorage")

    registerArtifact("output") {
        containerName.set("my-container")
        blobName.set("output/result.json")
        inputFile.set(layout.buildDirectory.file("result.json"))
    }
}
```

Each artifact is uploaded independently via `UploadBlobAction`. The task fails if any upload throws.

To use a client from outside `ClientsBaseService`, use `AbstractBatchUploadToAzureBlobStorage` directly and set
`service`/`clientName` on the abstract class.

## Work Actions

Individual blob operations for use with `WorkerExecutor.noIsolation()`:

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
            service.set(serviceClients.service)
            clientName.set("myAzureStorage")
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
| `service` | `Property<ClientsBaseService>` | The shared build service (set from `serviceClients.service`) |
| `clientName` | `Property<String>` | Registered name of a `BlobServiceClientInfo` |
| `containerName` | `Property<String>` | The blob container name |
| `blobName` / `prefix` | `Property<String>` | Blob name or optional prefix filter |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Azure Storage Blob SDK for Java](https://learn.microsoft.com/en-us/java/api/overview/azure/storage-blob-readme)
