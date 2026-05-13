# Artifactory Base

A Kotlin library providing managed JFrog Artifactory client integration, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:artifactory-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `ArtifactoryClientBuildService` | `Artifactory` (`org.jfrog.artifactory.client`) |

```kotlin
val artifactory = gradle.sharedServices.registerIfAbsent("artifactory", ArtifactoryClientBuildService::class) {
    parameters.url.set("https://mycompany.jfrog.io/artifactory")
    parameters.credentials.set(project.objects.newInstance<PasswordCredentials>().apply {
        username = "user"
        password = "token"
    })
}
```

| Parameter | Type | Description |
|---|---|---|
| `url` | `Property<String>` | Artifactory server URL |
| `credentials` | `Property<PasswordCredentials>` | Artifactory credentials (username + password/token) |

## Value Source: `AbstractArtifactValueSource`

Extend to download an artifact and transform the response:

```kotlin
abstract class MyValueSource : AbstractArtifactValueSource<String, AbstractArtifactValueSource.Parameters>() {
    override fun doObtain(input: InputStream): String = input.bufferedReader().readText()
}
```

Parameters: `service`, `repository`, `path`.

## Value Source: `AbstractStreamingRequestValueSource`

Extend to perform an arbitrary Artifactory REST call and transform the streaming response.

Parameters: `service`, `request` (an `ArtifactoryRequest`).

## WorkActions

### `DownloadArtifactAction`

Downloads a single artifact to a local file.

### `UploadArtifactAction`

Uploads a single file to an Artifactory repository.

## Tasks

### `BatchDownloadFromArtifactory` / `BatchUploadToArtifactory`

Download or upload multiple artifacts concurrently via `WorkerExecutor`. `AbstractBatchDownloadFromArtifactory`
/ `AbstractBatchUploadToArtifactory` are the same tasks minus the `@get:ServiceReference` annotation.

```kotlin
tasks.register<BatchDownloadFromArtifactory>("download") {
    service.set(artifactory)
    registerArtifact("config") { artifact ->
        artifact.repository.set("generic-local")
        artifact.path.set("config/settings.json")
        artifact.outputFile.set(layout.buildDirectory.file("config/settings.json"))
    }
}
```

**Note:** These tasks are intended for non-Maven, non-Ivy repositories (e.g. generic). For Maven/Ivy,
prefer Gradle's built-in dependency resolution and publishing mechanisms.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [jfrog-cli-core](../jfrog-cli-core) — JFrog CLI integration (an alternative for Artifactory operations)
