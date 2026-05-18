# Azure Container Registry Base

A Kotlin library providing managed Azure Container Registry client integration, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-container-registry-base")
}
```

## Build Services

Four build services are available, covering registry-level and repository-scoped access in both synchronous and
asynchronous variants:

| Class | Scope | Client |
|---|---|---|
| `ContainerRegistryClientBuildService` | Registry | `ContainerRegistryClient` |
| `ContainerRegistryAsyncClientBuildService` | Registry | `ContainerRegistryAsyncClient` |
| `ContainerRepositoryClientBuildService` | Single repository | `ContainerRepository` |
| `ContainerRepositoryAsyncClientBuildService` | Single repository | `ContainerRepositoryAsync` |

Each build service has its own `Params` interface extending `AzureBuildServiceParams` from
[azure-extensions](../azure-extensions); registry-scoped services add `endpoint` (registry URL), while
repository-scoped services chain from a registry-scoped service via `@ServiceReference`.

### Registry-level client

```kotlin
val registry = gradle.sharedServices.registerIfAbsent("acr", ContainerRegistryClientBuildService::class) {
    parameters {
        endpoint.set("https://myregistry.azurecr.io")
        defaultCredential()
        // managedIdentity()
        // clientSecret(tenantId, clientId, clientSecret)
    }
}
```

**Credential note:** Azure Container Registry supports `TokenCredential`-shaped credentials only.
The `sharedKey()` and `sasToken()` extension functions from `AzureBuildServiceParams` are **not supported**
and will throw `IllegalArgumentException` at client creation time.

### Repository-scoped client

```kotlin
val repository = gradle.sharedServices.registerIfAbsent("acr-myrepo", ContainerRepositoryClientBuildService::class) {
    parameters {
        registryService.set(registry)
        repositoryName.set("my-app")
    }
}
```

Repository-scoped services do **not** accept credential parameters; they inherit credentials from the
registry-scoped service they reference.

### Parameter reference

| Parameter | Type | Scope | Description |
|---|---|---|---|
| `endpoint` | `Property<String>` | Registry-scoped | Azure Container Registry endpoint URL, e.g. `https://{registryName}.azurecr.io` |
| `credentialSource` | `Property<AzureCredentialSource>` | Registry-scoped | Which credential to construct. Set via the extension functions. Leave unset to skip credential configuration. |
| `registryService` | `Property<ContainerRegistryClientBuildService>` | Repository-scoped | Reference to the registry-level build service |
| `repositoryName` | `Property<String>` | Repository-scoped | The name of the container repository |

Use the extension functions on `AzureBuildServiceParams` to configure credentials atomically (see
[azure-extensions](../azure-extensions)).

## Task: `BatchDeleteTagsFromAzureContainerRegistry`

Deletes a collection of tags from a repository concurrently via `WorkerExecutor.noIsolation()`. Set `service`
to a registered `ContainerRepositoryClientBuildService`:

```kotlin
tasks.register<BatchDeleteTagsFromAzureContainerRegistry>("cleanupOldTags") {
    service.set(repository)
    tags.set(setOf("snapshot-1", "snapshot-2", "snapshot-3"))
}
```

Each tag deletion is dispatched independently via `DeleteTagAction`. The task fails if any deletion throws.

## Task: `BatchDeleteManifestsFromAzureContainerRegistry`

Deletes untagged manifests by digest concurrently via `WorkerExecutor.noIsolation()`. Set `service`
to a registered `ContainerRepositoryClientBuildService`:

```kotlin
tasks.register<BatchDeleteManifestsFromAzureContainerRegistry>("pruneUntagged") {
    service.set(repository)
    digests.set(setOf("sha256:abc123...", "sha256:def456..."))
}
```

Each manifest deletion is dispatched independently via `DeleteManifestAction`. The task fails if any deletion throws.

## Work Actions

Individual container operations for use with `WorkerExecutor.noIsolation()`.

| Action | Service scope | Description |
|---|---|---|
| `DeleteRepositoryAction` | Registry-scoped | Deletes an entire repository |
| `UpdateRepositoryPropertiesAction` | Repository-scoped | Updates write/delete/list protection flags on a repository |
| `DeleteTagAction` | Repository-scoped | Deletes a specific tag from a repository |
| `UpdateTagPropertiesAction` | Repository-scoped | Updates write/delete flags on a tag |
| `DeleteManifestAction` | Repository-scoped | Deletes a manifest by digest |
| `UpdateManifestPropertiesAction` | Repository-scoped | Updates write/delete/list/read flags on a manifest |

## Value Sources

### `ListRepositoryNamesValueSource`

Lists all repository names in a registry. Use the registry-scoped build service:

```kotlin
val repos: Provider<List<String>> = providers.of(ListRepositoryNamesValueSource::class) {
    parameters {
        service.set(registry)
    }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ContainerRegistryClientBuildService>` | The build service supplying the registry-scoped Container Registry client |

### `ListTagNamesValueSource`

Lists all tag names in a repository. Use the repository-scoped build service:

```kotlin
val tags: Provider<List<String>> = providers.of(ListTagNamesValueSource::class) {
    parameters {
        service.set(repository)
    }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ContainerRepositoryClientBuildService>` | The build service supplying the repository-scoped Container Repository client |

### `ListManifestDigestsValueSource`

Lists manifest digests in a repository. Use the repository-scoped build service:

```kotlin
val digests: Provider<List<String>> = providers.of(ListManifestDigestsValueSource::class) {
    parameters {
        service.set(repository)
    }
}
```

Parameters:

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ContainerRepositoryClientBuildService>` | The build service supplying the repository-scoped Container Repository client |

**Configuration cache note:** Results are serialized to the Gradle configuration cache, but these ValueSources
only return non-sensitive string identifiers (repository names, tag names, digest hashes), so they are safe
to use at configuration time.

## See Also

- [azure-extensions](../azure-extensions) — Azure credential configuration utilities
- [clients-base](../clients-base) — The underlying service client infrastructure
- [Azure Container Registry SDK for Java](https://learn.microsoft.com/en-us/java/api/overview/azure/container-registry-readme)
