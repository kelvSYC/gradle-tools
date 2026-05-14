# Azure Extensions

A Gradle library providing config-cache-safe build service infrastructure for Azure SDK clients.

## Dependency

This library is a transitive dependency of the Azure Base plugins (`azure-blob-storage-base`,
`azure-key-vault-base`). Direct use is only needed when building plugins that define new Azure
client build services.

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-extensions")
}
```

## `AzureBuildServiceParams`

Config-cache-safe `BuildServiceParameters` interface for Azure SDK client build services. All
fields are serializable primitives (Strings, enums); use the extension functions below to configure
them rather than setting fields directly.

The service-specific endpoint (Storage account URL, Key Vault URL, etc.) is **not** part of this
interface — each service extends `AzureBuildServiceParams` with its own endpoint property because
the endpoint shape differs per service.

| Property | Type | Description |
|---|---|---|
| `credentialSource` | `Property<AzureCredentialSource>` | Which credential to construct. Leave unset to skip credential configuration. |
| `tenantId` | `Property<String>` | Azure AD tenant ID. Used by `CLIENT_SECRET`. |
| `clientId` | `Property<String>` | Azure AD application/client ID. Used by `CLIENT_SECRET` and (optionally) `MANAGED_IDENTITY`. |
| `clientSecret` | `Property<String>` | Azure AD client secret. Used by `CLIENT_SECRET`. |
| `sasToken` | `Property<String>` | SAS token (no leading `?`). Used by `SAS_TOKEN`. |
| `accountName` | `Property<String>` | Azure Storage account name. Used by `STORAGE_ACCOUNT_KEY`. |
| `accountKey` | `Property<String>` | Azure Storage account key. Used by `STORAGE_ACCOUNT_KEY`. |

### Extension functions

Configure an `AzureBuildServiceParams` instance atomically using one of these functions:

```kotlin
gradle.sharedServices.registerIfAbsent("blob", BlobServiceClientBuildService::class) {
    parameters {
        endpoint.set("https://myaccount.blob.core.windows.net")
        defaultCredential()                                                      // DefaultAzureCredential
        // managedIdentity()                                                     // System-assigned MI
        // managedIdentity("00000000-0000-0000-0000-000000000000")               // User-assigned MI
        // clientSecret(tenantId, clientId, clientSecret)                        // Service principal
        // sasToken("sv=2020-10-02&...")                                         // SAS token (Storage)
        // sharedKey("myaccount", accountKey)                                    // Account key (Storage)
        // noCredentials()                                                       // Skip credential configuration
    }
}
```

| Function | Credential result |
|---|---|
| `noCredentials()` | No credential configured |
| `defaultCredential()` | `DefaultAzureCredentialBuilder().build()` |
| `managedIdentity(clientId?)` | `ManagedIdentityCredentialBuilder()` (optionally with `clientId`) |
| `clientSecret(tenant, client, secret)` | `ClientSecretCredentialBuilder()` |
| `sasToken(token)` | `AzureSasCredential(token)` — Storage only |
| `sharedKey(accountName, accountKey)` | `AzureNamedKeyCredential(accountName, accountKey)` — Storage only |

## `AbstractAzureClientBuildService<C, P>`

Abstract base class for Azure SDK client build services. Extend this and implement `createClient()`,
pattern-matching on `resolveCredential()` to apply the subset of credential types supported by the
underlying Azure client builder. For services that accept all three credential shapes (Storage):

```kotlin
abstract class BlobServiceClientBuildService : AbstractAzureClientBuildService<BlobServiceClient, BlobServiceClientBuildService.Params>() {
    interface Params : AzureBuildServiceParams {
        val endpoint: Property<String>
    }

    override fun createClient(): BlobServiceClient = BlobServiceClientBuilder().apply {
        endpoint(parameters.endpoint.get())
        when (val credential = resolveCredential()) {
            null -> {}
            is ResolvedAzureCredential.Token -> credential(credential.credential)
            is ResolvedAzureCredential.Sas -> credential(credential.credential)
            is ResolvedAzureCredential.NamedKey -> credential(credential.credential)
        }
    }.buildClient()
}
```

For services that only accept `TokenCredential` (Key Vault), `resolveTokenCredential()` throws
`IllegalArgumentException` if the parameters request a Storage-only variant:

```kotlin
abstract class SecretClientBuildService : AbstractAzureClientBuildService<SecretClient, SecretClientBuildService.Params>() {
    interface Params : AzureBuildServiceParams {
        val vaultUrl: Property<String>
    }

    override fun createClient(): SecretClient = SecretClientBuilder().apply {
        vaultUrl(parameters.vaultUrl.get())
        resolveTokenCredential()?.let(::credential)
    }.buildClient()
}
```

| Method | Description |
|---|---|
| `resolveCredential(): ResolvedAzureCredential?` | Constructs the credential from `credentialSource`. Returns `null` for `NONE` and unset. |
| `resolveTokenCredential(): TokenCredential?` | Like `resolveCredential` but unwraps to the underlying `TokenCredential`. Throws if the parameters request a Storage-only variant. |

## `ResolvedAzureCredential`

A sealed wrapper distinguishing the three concrete credential families Azure client builders
accept:

| Variant | Class |
|---|---|
| `ResolvedAzureCredential.Token` | wraps `com.azure.core.credential.TokenCredential` |
| `ResolvedAzureCredential.Sas` | wraps `com.azure.core.credential.AzureSasCredential` |
| `ResolvedAzureCredential.NamedKey` | wraps `com.azure.core.credential.AzureNamedKeyCredential` |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Azure SDK for Java](https://learn.microsoft.com/en-us/java/api/overview/azure/)
