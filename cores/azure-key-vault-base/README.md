# Azure Key Vault Base

A Kotlin library providing managed Azure Key Vault client integration using the Azure SDK for Java, built on
`clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-key-vault-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `SecretClientBuildService` | `SecretClient` | Synchronous Key Vault secret operations |
| `SecretAsyncClientBuildService` | `SecretAsyncClient` | Asynchronous Key Vault secret operations |

Register a build service from a plugin or `build.gradle.kts`:

```kotlin
val kv = gradle.sharedServices.registerIfAbsent("kv", SecretClientBuildService::class) {
    parameters {
        vaultUrl.set("https://my-vault.vault.azure.net")
        defaultCredential()
        // managedIdentity()
        // clientSecret(tenantId, clientId, clientSecret)
    }
}
```

The parameter shape extends `AzureBuildServiceParams` from
[azure-extensions](../azure-extensions); use the extension functions on `AzureBuildServiceParams`
to configure credentials atomically. Key Vault only accepts `TokenCredential`-shaped credentials —
attempting to configure with `sasToken()` or `sharedKey()` throws `IllegalArgumentException` at
client construction time.

| Parameter | Type | Description |
|---|---|---|
| `vaultUrl` | `Property<String>` | Vault URL, e.g. `https://{vaultName}.vault.azure.net` |
| `credentialSource` | `Property<AzureCredentialSource>` | Which credential to construct. Set via the extension functions. Leave unset to skip credential configuration. |

## Value Source: `KeyVaultSecretValueSource`

Retrieves a secret from Azure Key Vault as a string.

```kotlin
val secret: Provider<String> = providers.of(KeyVaultSecretValueSource::class) {
    parameters {
        service.set(kv)
        secretName.set("my-secret")
        version.set("abc123")   // optional; defaults to latest
    }
}
```

Returns `null` (and logs a warning) if the call throws `HttpResponseException`.

## WorkAction: `SetSecretAction`

Stores a secret in Azure Key Vault. If the secret already exists, a new version is created.

```kotlin
workerExecutor.noIsolation().submit(SetSecretAction::class) {
    service.set(kv)
    secretName.set("my-secret")
    secretValue.set("new-value")
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [azure-blob-storage-base](../azure-blob-storage-base) — Azure Blob Storage variant
