# Azure Managed Identity Base

A Kotlin library providing managed Azure Managed Identity credential and Azure Instance Metadata
Service (IMDS) client integration using the Azure SDK for Java and Retrofit, built on `clients-base`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-managed-identity-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `ManagedIdentityCredentialBuildService` | `ManagedIdentityCredential` | Obtain OAuth2 tokens for Azure resources |
| `AzureImdsClientBuildService` | `AzureImdsService` (Retrofit) | Query Azure IMDS endpoints |

### `ManagedIdentityCredentialBuildService`

Wraps the Azure Identity SDK's `ManagedIdentityCredential`. Use the extension functions on its
`Params` to select system-assigned or user-assigned identity:

```kotlin
val mi = gradle.sharedServices.registerIfAbsent("mi", ManagedIdentityCredentialBuildService::class) {
    parameters.systemAssigned()  // no-op — system-assigned is the default
    // parameters.userAssigned("00000000-0000-0000-0000-000000000000")
    // parameters.userAssignedByObjectId("...")
    // parameters.userAssignedByResourceId("/subscriptions/.../resourceGroups/.../providers/...")
}
```

### `AzureImdsClientBuildService`

Creates a Retrofit client for the Azure Instance Metadata Service. No parameters are needed;
the IMDS endpoint is fixed at `http://169.254.169.254/metadata/`.

```kotlin
val imds = gradle.sharedServices.registerIfAbsent("imds", AzureImdsClientBuildService::class) {}
```

## Value Sources

### `AccessTokenValueSource`

Retrieves a raw OAuth2 bearer token for the specified Azure scopes.

```kotlin
val token: Provider<String> = providers.of(AccessTokenValueSource::class) {
    parameters {
        service.set(mi)
        scopes.add("https://management.azure.com/.default")
    }
}
```

### `AzureComputeMetadataValueSource`

Queries `/instance/compute` and returns a `Map<String, String>` of non-null compute metadata fields
(`subscriptionId`, `resourceGroupName`, `name`, `location`, `vmId`, `vmSize`, `osType`).

```kotlin
val compute: Provider<Map<String, String>> = providers.of(AzureComputeMetadataValueSource::class) {
    parameters {
        service.set(imds)
        apiVersion.set("2021-02-01")  // optional; defaults to 2021-02-01
    }
}
```

### `GetManagedIdentityInfoValueSource`

Queries `/identity/info` and returns a `Map<String, String>` with keys `clientId` and `objectId`.

```kotlin
val identityInfo: Provider<Map<String, String>> = providers.of(GetManagedIdentityInfoValueSource::class) {
    parameters {
        service.set(imds)
        apiVersion.set("2018-02-01")  // optional; defaults to 2018-02-01
    }
}
```

### `AzureAttestedDataValueSource`

Queries `/attested/document` and returns the raw `signature` string (base64-encoded PKCS#7 document).

```kotlin
val signature: Provider<String> = providers.of(AzureAttestedDataValueSource::class) {
    parameters {
        service.set(imds)
        apiVersion.set("2021-02-01")
        nonce.set("optional-nonce")  // optional
    }
}
```

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [azure-blob-storage-base](../azure-blob-storage-base) — Azure Blob Storage variant
- [azure-key-vault-base](../azure-key-vault-base) — Azure Key Vault variant
