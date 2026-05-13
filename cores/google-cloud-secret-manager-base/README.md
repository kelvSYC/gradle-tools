# google-cloud-secret-manager-base

A Kotlin library providing managed Google Cloud Secret Manager client integration, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:google-cloud-secret-manager-base")
}
```

## Build Service

| Class | Client type |
|---|---|
| `SecretManagerServiceClientBuildService` | `SecretManagerServiceClient` |

Register the build service from a plugin or `build.gradle.kts`:

```kotlin
val sm = gradle.sharedServices.registerIfAbsent("sm", SecretManagerServiceClientBuildService::class) {
    // credentials is optional; omit to use application default credentials
    parameters.credentials.set(GoogleCredentials.getApplicationDefault())
}
```

The GCP project ID is not part of the build service — each value source and action takes its own `projectId`
parameter, so a single client can serve calls against multiple projects.

## Value Source: `SecretManagerValueSource`

Retrieves a secret version payload as a UTF-8 string.

```kotlin
val secret: Provider<String> = providers.of(SecretManagerValueSource::class) {
    parameters {
        service.set(sm)
        projectId.set("my-gcp-project")
        secretId.set("my-secret")
        versionId.set("1")   // optional; defaults to "latest"
    }
}
```

Returns `null` (and logs a warning) if the call throws `ApiException`.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<SecretManagerServiceClientBuildService>` | The shared build service |
| `projectId` | `Property<String>` | GCP project ID |
| `secretId` | `Property<String>` | Secret ID |
| `versionId` | `Property<String>` | Secret version; defaults to `"latest"` |

## Value Source: `ListSecretsValueSource`

Returns the fully-qualified resource names of every secret in a project
(`projects/{project}/secrets/{secret}`). Pagination is handled internally.

```kotlin
val secretNames: Provider<List<String>> = providers.of(ListSecretsValueSource::class) {
    parameters {
        service.set(sm)
        projectId.set("my-gcp-project")
    }
}
```

## WorkAction: `AddSecretVersionAction`

Adds a new version to an existing secret. Only string (UTF-8) payloads are supported.

```kotlin
workerExecutor.noIsolation().submit(AddSecretVersionAction::class) {
    service.set(sm)
    projectId.set("my-gcp-project")
    secretId.set("my-secret")
    payload.set("new-secret-value")
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
