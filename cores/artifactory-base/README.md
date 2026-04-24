# Artifactory Base

A Gradle plugin providing managed Artifactory client integration for build scripts and custom Gradle plugins.

## Overview

`artifactory-base` simplifies interaction with Artifactory repositories in Gradle builds by:

- **Centralizing client management**: Register named Artifactory clients with credentials via `serviceClients`, then retrieve them anywhere in your build
- **On-demand instantiation**: Clients are created only when accessed, and reused throughout the build lifecycle
- **Provider-first design**: Value sources integrate with Gradle's `Provider` system for lazy, cached evaluation in build configuration
- **Extensibility**: Base classes for building custom value sources that read data from Artifactory into memory (artifact content, REST API responses)

This plugin is a foundation for other build infrastructure that needs Artifactory access—such as plugins that query release artifact metadata, query repository information, or perform cross-repository operations.

## Quick Start

### 1. Apply the Plugin

In your `build.gradle.kts`:

```kotlin
plugins {
    id("com.kelvsyc.gradle.artifactory-base") version "0.1.0.0"
}
```

### 2. Configure a Client

Use the `serviceClients` extension to register an Artifactory client. Credentials are read from Gradle
properties `myArtifactoryUsername` and `myArtifactoryPassword` (see
[Gradle PasswordCredentials](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:handling_credentials)):

```kotlin
val artifactoryCredentials = providers.credentials(PasswordCredentials::class.java, "myArtifactory")

serviceClients.service.get().registerIfAbsent<ArtifactoryClientInfo>("myArtifactory") {
    url.set("https://artifactory.example.com")
    credentials.set(artifactoryCredentials)
}
```

### 3. Use the Client

Retrieve the client as a lazy `Provider` via the `serviceClients` extension:

```kotlin
tasks.register("queryArtifactory") {
    val client: Provider<Artifactory> =
        serviceClients.getClient<Artifactory, ArtifactoryClientInfo>("myArtifactory")

    doLast {
        val artifactory = client.get()
        // Perform Artifactory operations with the client
    }
}
```

Use `.map()` to derive a typed value from the client at execution time:

```kotlin
tasks.register("queryArtifactory") {
    val repositoryNames: Provider<List<LightweightRepository>> =
        serviceClients.getClient<Artifactory, ArtifactoryClientInfo>("myArtifactory")
            .map { it.repositories.list(null) }

    doLast {
        println("Repositories: ${repositoryNames.get()}")
    }
}
```

## Building Custom Integrations

### Value Sources for Reading Artifacts into Memory

Extend `AbstractArtifactValueSource` to create reusable value sources that read artifact content from Artifactory
into memory and transform it:

```kotlin
import com.kelvsyc.gradle.artifactory.AbstractArtifactValueSource
import java.io.InputStream

abstract class MyArtifactValueSource : AbstractArtifactValueSource<String, MyArtifactValueSource.Parameters>() {
    interface Parameters : AbstractArtifactValueSource.Parameters {
        // Add custom parameters here if needed
    }

    override fun doObtain(input: InputStream): String =
        input.bufferedReader().use { it.readText() }
}
```

Use it in task configuration to read artifact content into memory:

```kotlin
tasks.register("readArtifactContent") {
    val content: Provider<String> = providers.of(MyArtifactValueSource::class) {
        parameters {
            service.set(serviceClients.service)
            clientName.set("myArtifactory")
            repository.set("my-repo")
            path.set("path/to/artifact.txt")
        }
    }

    doLast {
        println("Content: ${content.get()}")
    }
}
```

### Value Sources for Streaming REST API Calls

Extend `AbstractStreamingRequestValueSource` to create reusable value sources that query Artifactory via REST API:

```kotlin
import com.kelvsyc.gradle.artifactory.AbstractStreamingRequestValueSource
import org.jfrog.artifactory.client.ArtifactoryStreamingResponse

abstract class MyRestValueSource : AbstractStreamingRequestValueSource<String, MyRestValueSource.Parameters>() {
    interface Parameters : AbstractStreamingRequestValueSource.Parameters {
        // Add custom parameters here if needed
    }

    override fun doObtain(response: ArtifactoryStreamingResponse): String =
        response.stream.bufferedReader().use { it.readText() }
}
```

Use it in task configuration:

```kotlin
tasks.register("callArtifactoryApi") {
    val response: Provider<String> = providers.of(MyRestValueSource::class) {
        parameters {
            service.set(serviceClients.service)
            clientName.set("myArtifactory")
            request.set(
                ArtifactoryRequestImpl()
                    .method(ArtifactoryRequest.Method.GET)
                    .apiUrl("api/repositories")
                    .responseType(ArtifactoryRequest.ContentType.JSON)
            )
        }
    }

    doLast {
        println("Response: ${response.get()}")
    }
}
```

## Core Classes

### `ArtifactoryClientInfo`

Configuration container for an Artifactory client registration:

- **`url`** (`Property<String>`): The base URL of your Artifactory instance
- **`credentials`** (`Property<PasswordCredentials>`): Username and password for authentication

### `AbstractArtifactValueSource<T, P>`

Base class for value sources that read artifact content from Artifactory into memory. Subclasses must implement:

- **`doObtain(input: InputStream): T?`** — Read and transform the artifact stream into your desired in-memory type

Parameters (declared in `AbstractArtifactValueSource.Parameters`):

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service (set from `serviceClients.service`) |
| `clientName` | `Property<String>` | The registered client name |
| `repository` | `Property<String>` | The Artifactory repository key |
| `path` | `Property<String>` | The path to the artifact within the repository |

### `AbstractStreamingRequestValueSource<T, P>`

Base class for value sources that make REST API calls to Artifactory. Subclasses must implement:

- **`doObtain(response: ArtifactoryStreamingResponse): T?`** — Transform the API response into your desired type

Parameters (declared in `AbstractStreamingRequestValueSource.Parameters`):

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service (set from `serviceClients.service`) |
| `clientName` | `Property<String>` | The registered client name |
| `request` | `Property<ArtifactoryRequest>` | The REST request to execute |

## Architecture

This plugin builds on top of `clients-base`, which provides a polymorphic service registry exposed via the
`serviceClients` extension. When you apply `artifactory-base`:

1. The `clients-base` plugin is automatically applied
2. `ArtifactoryClientInfo` is registered as a creatable type in the `ClientsBaseService`
3. Clients are registered via `serviceClients.service.get().registerIfAbsent<ArtifactoryClientInfo>()` and retrieved
   via `serviceClients.getClient<Artifactory, ArtifactoryClientInfo>()`

The `serviceClients` extension is a `ClientsBaseExtension` backed by a shared `ClientsBaseService` build service.
Client instances are created on first access and cached for the remainder of the build.

This design allows multiple service client types (S3, GCP Storage, etc.) to coexist in the same build with
independent credentials and lifecycle management.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Gradle PasswordCredentials](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:handling_credentials) — Gradle documentation on credential handling
- [Gradle Providers and ValueSources](https://docs.gradle.org/current/userguide/lazy_configuration.html) — Gradle documentation on lazy evaluation
- [JFrog Artifactory Java Client](https://github.com/jfrog/artifactory-client-java) — The underlying Artifactory client library