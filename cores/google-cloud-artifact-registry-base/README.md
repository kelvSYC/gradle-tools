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

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Google Cloud Artifact Registry Java Client](https://cloud.google.com/java/docs/reference/google-cloud-artifact-registry/latest/overview)