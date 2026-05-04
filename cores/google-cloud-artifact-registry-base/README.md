# Google Cloud Artifact Registry Base

A Gradle plugin providing managed Google Cloud Artifact Registry client integration.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.google-cloud-artifact-registry-base")
}
```

## Client Registration

The client info type is `ArtifactRegistryClientInfo`. Use the `registerGoogleCloudServiceClient` convenience
extension:

```kotlin
serviceClients.registerGoogleCloudServiceClient("myArtifactRegistry") {
    credentials.set(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault()))
}
```

### `ArtifactRegistryClientInfo` properties

| Property | Type | Description |
|---|---|---|
| `credentials` | `Property<CredentialsProvider>` | Google API `CredentialsProvider` for authentication. Required. |

> **Note:** The `registerGoogleCloudServiceClient` function name is also used in `google-cloud-storage-base` for
> `StorageClientInfo`. If both plugins are applied, prefer calling `serviceClients.service.get().registerIfAbsent<ArtifactRegistryClientInfo>(...)` directly to avoid ambiguity.

## Value Source: `AbstractArtifactValueSource`

Extend `AbstractArtifactValueSource` to read a file from Artifact Registry into memory:

```kotlin
abstract class MyArtifactValueSource
    : AbstractArtifactValueSource<String, AbstractArtifactValueSource.Parameters>() {

    override fun doObtain(input: InputStream): String? =
        input.bufferedReader().use { it.readText() }
}
```

Use it in task configuration:

```kotlin
tasks.register("readFromArtifactRegistry") {
    val content: Provider<String> = providers.of(MyArtifactValueSource::class) {
        parameters {
            service.set(serviceClients.service)
            clientName.set("myArtifactRegistry")
            projectName.set("my-gcp-project")
            location.set("us-east1")
            repository.set("my-repo")
            filename.set("path/to/file.txt")
        }
    }

    doLast { println(content.get()) }
}
```

The file is streamed via a `PipedInputStream`/`PipedOutputStream` pair using `runBlocking` with a coroutine, so
`doObtain` is called concurrently with the download.

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service (set from `serviceClients.service`) |
| `clientName` | `Property<String>` | Registered name of an `ArtifactRegistryClientInfo` |
| `projectName` | `Property<String>` | GCP project ID |
| `location` | `Property<String>` | Artifact Registry location (e.g. `us-east1`) |
| `repository` | `Property<String>` | Repository name |
| `filename` | `Property<String>` | File path within the repository |

## Value Source: `GetRepositoryValueSource`

Returns the [`Repository`](https://cloud.google.com/java/docs/reference/google-cloud-artifact-registry/latest/com.google.devtools.artifactregistry.v1.Repository)
proto for the named repository, exposing format, mode, description, labels, and other metadata.

```kotlin
val repository: Provider<Repository> = providers.of(GetRepositoryValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("myArtifactRegistry")
        projectName.set("my-gcp-project")
        location.set("us-east1")
        repository.set("my-repo")
    }
}
```

## Value Source: `ListPackagesValueSource`

Returns the fully-qualified resource names of every package in a repository. Pagination is handled internally.

```kotlin
val packages: Provider<List<String>> = providers.of(ListPackagesValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("myArtifactRegistry")
        projectName.set("my-gcp-project")
        location.set("us-east1")
        repository.set("my-repo")
    }
}
```

## Value Source: `ListVersionsValueSource`

Returns the fully-qualified resource names of every version of a given package.

```kotlin
val versions: Provider<List<String>> = providers.of(ListVersionsValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("myArtifactRegistry")
        projectName.set("my-gcp-project")
        location.set("us-east1")
        repository.set("my-repo")
        packageName.set("my-package")
    }
}
```

## Value Source: `ListFilesValueSource`

Returns the fully-qualified resource names of files in a repository, with an optional filter expression.

```kotlin
val files: Provider<List<String>> = providers.of(ListFilesValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("myArtifactRegistry")
        projectName.set("my-gcp-project")
        location.set("us-east1")
        repository.set("my-repo")
        // Optional — narrow to files owned by a specific package version.
        filter.set("owner=\"projects/my-gcp-project/locations/us-east1/repositories/my-repo/packages/my-package/versions/1.0.0\"")
    }
}
```

## Work Action: `DownloadFileAction`

Companion to `AbstractArtifactValueSource` that writes the `GetFile` response to a `RegularFileProperty`,
matching the action-style download primitives in the AWS bases. Suitable for dispatch via `WorkerExecutor`.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of an `ArtifactRegistryClientInfo` |
| `projectName` | `Property<String>` | GCP project ID |
| `location` | `Property<String>` | Artifact Registry location |
| `repository` | `Property<String>` | Repository name |
| `filename` | `Property<String>` | File path within the repository |
| `outputFile` | `RegularFileProperty` | Destination file |

> **Note:** Generic-artifact upload is not exposed by the Artifact Registry Java client and is not yet
> implemented in this base.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Google Cloud Artifact Registry Java Client](https://cloud.google.com/java/docs/reference/google-cloud-artifact-registry/latest/overview)