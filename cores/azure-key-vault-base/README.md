# Azure Key Vault Base

A Gradle plugin providing managed Azure Key Vault client integration using the Azure SDK for Java.

## Applying the Plugin

```kotlin
plugins {
    id("com.kelvsyc.gradle.azure-key-vault-base")
}
```

## Client Types

Two client info types are registered:

| Client info type | Client type |
|---|---|
| `SecretClientInfo` | `SecretClient` (synchronous) |
| `SecretAsyncClientInfo` | `SecretAsyncClient` (asynchronous) |

Both extend `AzureKeyVaultClientInfo`, which exposes the following properties:

| Property | Type | Description |
|---|---|---|
| `vaultUrl` | `Property<String>` | The vault URL, e.g. `https://{vaultName}.vault.azure.net` |
| `credential` | `Property<TokenCredential>` | Azure credentials (optional) |

Register a client:

```kotlin
serviceClients.registerAzureKeyVaultSecretClient("keyVault") {
    vaultUrl.set("https://my-vault.vault.azure.net")
    credential.set(DefaultAzureCredentialBuilder().build())
}
```

Or using the service directly:

```kotlin
serviceClients.service.get().registerIfAbsent<SecretClientInfo>("keyVault") {
    vaultUrl.set("https://my-vault.vault.azure.net")
    credential.set(DefaultAzureCredentialBuilder().build())
}
```

## Value Sources

### `KeyVaultSecretValueSource`

Retrieves a single secret from Azure Key Vault:

```kotlin
val secret: Provider<String> = providers.of(KeyVaultSecretValueSource::class) {
    parameters {
        service.set(serviceClients.service)
        clientName.set("keyVault")
        secretName.set("my-secret")
    }
}
```

An optional `version` parameter can be set to retrieve a specific secret version. If omitted,
the latest version is returned.

Returns `null` and logs a warning if the call throws `HttpResponseException`.

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `SecretClientInfo` |
| `secretName` | `Property<String>` | The name of the secret to retrieve |
| `version` | `Property<String>` | The secret version (optional; defaults to latest) |

## WorkActions

### `SetSecretAction`

Stores a secret in Azure Key Vault. If the secret already exists, a new version is created:

```kotlin
workerExecutor.noIsolation().submit(SetSecretAction::class) {
    service.set(serviceClients.service)
    clientName.set("keyVault")
    secretName.set("my-secret")
    secretValue.set("{\"username\":\"admin\",\"password\":\"newPassword\"}")
}
```

| Parameter | Type | Description |
|---|---|---|
| `service` | `Property<ClientsBaseService>` | The shared build service |
| `clientName` | `Property<String>` | Registered name of a `SecretClientInfo` |
| `secretName` | `Property<String>` | The name of the secret to set |
| `secretValue` | `Property<String>` | The secret value to store |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [azure-blob-storage-base](../azure-blob-storage-base) — Azure Blob Storage integration
