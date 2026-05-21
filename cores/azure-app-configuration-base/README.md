# Azure App Configuration Base

A Kotlin library providing managed Azure App Configuration client integration using the Azure SDK for Java, built on `clients-base`. Supports key-value settings, first-class feature flags, configuration snapshots, and optional management-plane store lifecycle operations.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:azure-app-configuration-base")
}
```

## Build Services

| Class | Client type | Use case |
|---|---|---|
| `AppConfigurationClientBuildService` | `ConfigurationClient` | Synchronous data-plane operations |
| `AppConfigurationAsyncClientBuildService` | `ConfigurationAsyncClient` | Asynchronous data-plane operations |
| `AppConfigurationManagerBuildService` | `AppConfigurationManager` | Management-plane store lifecycle |

### Data-plane services

```kotlin
val appconfig = gradle.sharedServices.registerIfAbsent("appconfig", AppConfigurationClientBuildService::class) {
    parameters {
        endpoint.set("https://my-store.azconfig.io")
        defaultCredential()
        // managedIdentity()
        // clientSecret(tenantId, clientId, clientSecret)
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `endpoint` | `Property<String>` | Store endpoint URL. Optional when `connectionStringRef` is set. |
| `connectionStringRef` | `Property<CredentialReference>` | Connection string reference (see below). Takes priority over `endpoint`. |
| `credentialSource` | `Property<AzureCredentialSource>` | Token credential to use. Set via extension functions. |

### Management-plane service

```kotlin
val manager = gradle.sharedServices.registerIfAbsent("appconfig-mgr", AppConfigurationManagerBuildService::class) {
    parameters {
        subscriptionId.set("00000000-0000-0000-0000-000000000000")
        resourceGroup.set("my-resource-group")
        defaultCredential()
    }
}
```

| Parameter | Type | Description |
|---|---|---|
| `subscriptionId` | `Property<String>` | Azure subscription ID. |
| `resourceGroup` | `Property<String>` | Resource group containing the stores. |
| `credentialSource` | `Property<AzureCredentialSource>` | Token credential. Falls back to `DefaultAzureCredential` when absent. |

## Pull (ValueSources)

### Data-plane ValueSources

**`GetConfigurationSettingValueSource`** — Returns the value of a single key-value setting, or `null` if not found or if the key is a Key Vault reference.

```kotlin
val value: Provider<String> = providers.of(GetConfigurationSettingValueSource::class) {
    parameters {
        service.set(appconfig)
        key.set("app.name")
        label.set("prod")   // optional
    }
}
```

**`ListConfigurationSettingsValueSource`** — Returns a `Map<String, String>` of key → value for all matching settings. Key Vault references are excluded.

```kotlin
val settings: Provider<Map<String, String>> = providers.of(ListConfigurationSettingsValueSource::class) {
    parameters {
        service.set(appconfig)
        keyFilter.set("app.*")        // optional
        labelFilter.set("prod")       // optional
    }
}
```

**`GetFeatureFlagValueSource`** — Returns `true`/`false` for a feature flag, or `null` if not found.

```kotlin
val enabled: Provider<Boolean> = providers.of(GetFeatureFlagValueSource::class) {
    parameters {
        service.set(appconfig)
        featureName.set("new-ui")
        label.set("prod")   // optional
    }
}
```

**`ListFeatureFlagsValueSource`** — Returns a `Map<String, Boolean>` of feature flag name → enabled state.

```kotlin
val flags: Provider<Map<String, Boolean>> = providers.of(ListFeatureFlagsValueSource::class) {
    parameters {
        service.set(appconfig)
        label.set("prod")   // optional
    }
}
```

**`ListSnapshotsValueSource`** — Returns a `List<String>` of snapshot names currently in `READY` state.

```kotlin
val snapshots: Provider<List<String>> = providers.of(ListSnapshotsValueSource::class) {
    parameters {
        service.set(appconfig)
        nameFilter.set("release-*")   // optional
    }
}
```

### Management-plane ValueSources

**`GetConfigurationStoreValueSource`** — Returns the endpoint URL for a store, or `null` if not found or still provisioning.

```kotlin
val endpoint: Provider<String> = providers.of(GetConfigurationStoreValueSource::class) {
    parameters {
        service.set(manager)
        storeName.set("my-store")
    }
}
```

**`ListConfigurationStoresValueSource`** — Returns a `Map<String, String>` of store name → endpoint URL for all stores in the resource group. Stores with no endpoint (still provisioning) are excluded.

```kotlin
val stores: Provider<Map<String, String>> = providers.of(ListConfigurationStoresValueSource::class) {
    parameters {
        service.set(manager)
    }
}
```

## Push (WorkActions)

### Data-plane WorkActions

**`SetConfigurationSettingAction`** — Creates or updates a key-value setting.

```kotlin
workerExecutor.noIsolation().submit(SetConfigurationSettingAction::class) {
    service.set(appconfig)
    key.set("app.version")
    value.set("1.2.3")
    label.set("prod")         // optional
    contentType.set("text/plain")  // optional
}
```

**`DeleteConfigurationSettingAction`** — Deletes a key-value setting. No-op if not found.

```kotlin
workerExecutor.noIsolation().submit(DeleteConfigurationSettingAction::class) {
    service.set(appconfig)
    key.set("app.deprecated")
    label.set("prod")   // optional
}
```

**`SetFeatureFlagAction`** — Creates or updates a feature flag.

```kotlin
workerExecutor.noIsolation().submit(SetFeatureFlagAction::class) {
    service.set(appconfig)
    featureName.set("new-ui")
    enabled.set(true)
    label.set("prod")                         // optional
    description.set("Enable new UI redesign") // optional
}
```

**`DeleteFeatureFlagAction`** — Deletes a feature flag.

```kotlin
workerExecutor.noIsolation().submit(DeleteFeatureFlagAction::class) {
    service.set(appconfig)
    featureName.set("old-ui")
    label.set("prod")   // optional
}
```

**`CreateSnapshotAction`** — Creates a configuration snapshot (long-running operation; blocks until complete).

```kotlin
workerExecutor.noIsolation().submit(CreateSnapshotAction::class) {
    service.set(appconfig)
    snapshotName.set("release-1.2.3")
    keyFilter.set("app.*")
    labelFilter.set("prod")          // optional
    retentionPeriod.set(2592000L)    // optional, seconds (default: SDK default)
}
```

**`ArchiveSnapshotAction`** — Archives a snapshot.

```kotlin
workerExecutor.noIsolation().submit(ArchiveSnapshotAction::class) {
    service.set(appconfig)
    snapshotName.set("release-1.2.0")
}
```

**`RecoverSnapshotAction`** — Recovers an archived snapshot.

```kotlin
workerExecutor.noIsolation().submit(RecoverSnapshotAction::class) {
    service.set(appconfig)
    snapshotName.set("release-1.2.0")
}
```

### Management-plane WorkActions

**`CreateConfigurationStoreAction`** — Creates a new App Configuration store.

```kotlin
workerExecutor.noIsolation().submit(CreateConfigurationStoreAction::class) {
    service.set(manager)
    storeName.set("my-new-store")
    location.set("eastus")
    sku.set("Standard")   // optional, defaults to "Free"
}
```

**`DeleteConfigurationStoreAction`** — Deletes an App Configuration store.

```kotlin
workerExecutor.noIsolation().submit(DeleteConfigurationStoreAction::class) {
    service.set(manager)
    storeName.set("my-old-store")
}
```

## Connection String Authentication

For local development or CI environments without managed identity, authenticate using an App Configuration connection string:

```kotlin
val appconfig = gradle.sharedServices.registerIfAbsent("appconfig", AppConfigurationClientBuildService::class) {
    parameters {
        connectionStringRef.set(CredentialReference.ofEnvVar("APPCONFIG_CONNECTION_STRING"))
    }
}
```

When `connectionStringRef` is set, the `endpoint` and credential source are ignored — the connection string contains both the endpoint and the key. Do not store raw connection strings in `build.gradle.kts`; use an environment variable reference via `CredentialReference.ofEnvVar()` or `CredentialReference.ofSystemProperty()`.

**Note on Key Vault references:** Settings whose content type marks them as Key Vault references are intentionally excluded from `GetConfigurationSettingValueSource` and `ListConfigurationSettingsValueSource`. Resolving Key Vault references at configuration time would write secret values to the Gradle configuration cache. Resolve Key Vault references inside a `WorkAction` using [azure-key-vault-base](../azure-key-vault-base) at task execution time instead.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [azure-extensions](../azure-extensions) — Azure credential helpers and `AzureBuildServiceParams`
- [azure-key-vault-base](../azure-key-vault-base) — Resolve Key Vault references at execution time
