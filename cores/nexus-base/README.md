# Nexus Base

A Kotlin library providing managed Sonatype Nexus Repository Manager 3 client infrastructure,
built on `clients-base` with a Retrofit/OkHttp transport layer.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:nexus-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `NexusClientBuildService` | `NexusService` (internal Retrofit interface) |

Register an instance via `BuildServiceRegistry.registerIfAbsent`, then share it across value
sources and work actions.

### Anonymous (unauthenticated) access

```kotlin
val nexus = gradle.sharedServices.registerIfAbsent("nexus", NexusClientBuildService::class) {
    parameters.baseUrl.set("https://nexus.example.com/")
    parameters.anonymous()
}
```

### Basic authentication (username + password / user token)

```kotlin
val nexus = gradle.sharedServices.registerIfAbsent("nexus", NexusClientBuildService::class) {
    parameters.baseUrl.set("https://nexus.example.com/")
    parameters.basicAuth(
        username = "ci-user",
        // Defaults to CredentialReference.EnvironmentVariable("NEXUS_PASSWORD")
    )
}
```

Nexus user tokens (generated via **Security → User Tokens** in the Nexus UI) are presented as a
username/password pair and consumed through `basicAuth()` without API difference.

| Parameter | Type | Required | Description |
|---|---|---|---|
| `baseUrl` | `Property<String>` | Yes | Base URL of your Nexus instance (include trailing `/`) |
| `username` | `Property<String>` | No | Username; leave unset for anonymous access |
| `passwordRef` | `Property<CredentialReference>` | No | Credential reference for the password; set via `basicAuth()` |

## Value Source: `AbstractNexusArtifactValueSource`

Extend to download an artifact from a raw repository and transform the bytes at configuration time:

```kotlin
abstract class VersionManifestValueSource :
    AbstractNexusArtifactValueSource<String>() {
    override fun doObtain(input: InputStream): String = input.bufferedReader().readText()
}

// In your plugin or build script:
val manifest = providers.of(VersionManifestValueSource::class) {
    parameters.service.set(nexus)
    parameters.repository.set("generic-local")
    parameters.path.set("releases/manifest.txt")
}
```

> **Configuration cache and sensitive artifacts:** `AbstractNexusArtifactValueSource` serializes
> its result to the Gradle configuration cache in plaintext. Do not use it to fetch artifacts whose
> contents are sensitive (credentials, private keys, tokens). Fetch sensitive content inside a
> `WorkAction` at task execution time instead — the result is resolved after the cache has been
> read and is never written to it.

## WorkActions

### `DownloadArtifactAction`

Downloads a single artifact from a Nexus raw repository to a local file. The response body is
streamed to avoid buffering large artifacts in heap.

### `UploadArtifactAction`

Uploads a single local file to a Nexus raw repository. The target `path` is split on the last
`/` to derive `raw.directory` and `raw.asset1.filename` for the Nexus REST v1 multipart format.

## Tasks

### `BatchDownloadFromNexus` / `BatchUploadToNexus`

Download or upload multiple artifacts concurrently via `WorkerExecutor.noIsolation()`.
`AbstractBatchDownloadFromNexus` / `AbstractBatchUploadToNexus` are the same tasks minus the
`@get:ServiceReference` annotation, for use when you manage the service property yourself.

```kotlin
tasks.register<BatchDownloadFromNexus>("downloadAssets") {
    service.set(nexus)
    registerArtifact("settings") { artifact ->
        artifact.repository.set("generic-local")
        artifact.path.set("config/settings.json")
        artifact.outputFile.set(layout.buildDirectory.file("config/settings.json"))
    }
    registerArtifact("schema") { artifact ->
        artifact.repository.set("generic-local")
        artifact.path.set("schema/api.json")
        artifact.outputFile.set(layout.buildDirectory.file("schema/api.json"))
    }
}

tasks.register<BatchUploadToNexus>("publishAssets") {
    service.set(nexus)
    registerArtifact("dist") { artifact ->
        artifact.repository.set("generic-releases")
        artifact.path.set("com/example/1.0/dist-1.0.zip")
        artifact.inputFile.set(layout.buildDirectory.file("distributions/dist-1.0.zip"))
    }
}
```

## See Also

- [clients-base](../clients-base) — The underlying build service and credential infrastructure
- [artifactory-base](../artifactory-base) — JFrog Artifactory (same pattern, native SDK)
