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
    parameters {
        applicationDefault()
        // serviceAccount(layout.projectDirectory.file("service-account.json"))
        // noCredentials()
    }
}
```

The parameter shape is provided by `GcpBuildServiceParams` from
[google-cloud-extensions](../google-cloud-extensions); use the extension functions on
`GcpBuildServiceParams` to configure credentials atomically. Leave `credentialSource` unset to fall
back to the SDK's default application-default-credential resolution.

The GCP project ID is not part of the build service — each value source and action takes its own `projectId`
parameter, so a single client can serve calls against multiple projects.

## Value Source: **Deprecated.** `SecretManagerValueSource`

**Note on configuration cache safety:** Gradle serializes the result of every `ValueSource.obtain()` call to the configuration cache in plaintext. Any credential or secret value returned by a `ValueSource` will be stored in `.gradle/configuration-cache/` and is readable by any process with access to the build directory. The `ValueSource` implementations in this component that retrieve credentials or secrets are deprecated for this reason. Retrieve sensitive values inside a `WorkAction` at task execution time instead — the value is resolved after the cache has been read and is never written to it.

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
