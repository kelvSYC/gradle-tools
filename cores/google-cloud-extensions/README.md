# Google Cloud Extensions

A Gradle library providing config-cache-safe build service infrastructure for Google Cloud SDK clients.

## Dependency

This library is a transitive dependency of the Google Cloud Base plugins (`google-cloud-storage-base`,
`google-cloud-secret-manager-base`, `google-cloud-pubsub-base`, `google-cloud-artifact-registry-base`).
Direct use is only needed when building plugins that define new Google Cloud client build services.

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:google-cloud-extensions")
}
```

## `GcpBuildServiceParams`

Config-cache-safe `BuildServiceParameters` interface for Google Cloud client build services. All fields
are serializable primitives (Strings, files, enums); use the extension functions below to configure them
rather than setting fields directly.

| Property | Type | Description |
|---|---|---|
| `projectId` | `Property<String>` | GCP project ID. Leave unset to delegate to the SDK's default project resolution (`GOOGLE_CLOUD_PROJECT`, `gcloud config`, …). |
| `credentialSource` | `Property<GcpCredentialSource>` | Which credentials object to construct. Leave unset for the SDK's default resolution. |
| `credentialsFile` | `RegularFileProperty` | Service account JSON key file. Used when `credentialSource` is `SERVICE_ACCOUNT_JSON_FILE`. |
| `credentialsJson` | `Property<String>` | Inline service account JSON payload. Used when `credentialSource` is `SERVICE_ACCOUNT_JSON_INLINE`. |
| `accessToken` | `Property<String>` | Static OAuth2 access token. Used when `credentialSource` is `ACCESS_TOKEN`. |

### Extension functions

Configure a `GcpBuildServiceParams` instance atomically using one of these functions:

```kotlin
gradle.sharedServices.registerIfAbsent("storage", StorageClientBuildService::class) {
    parameters {
        projectId.set("my-project")
        applicationDefault()                                                            // GoogleCredentials.getApplicationDefault()
        // noCredentials()                                                              // NoCredentials.getInstance()
        // serviceAccount(layout.projectDirectory.file("service-account.json"))         // From JSON file
        // serviceAccount(providers.environmentVariable("SERVICE_ACCOUNT_JSON"))        // From inline JSON
        // accessToken("ya29.a0...")                                                    // From a static OAuth2 token
    }
}
```

| Function | Credential result |
|---|---|
| `noCredentials()` | `NoCredentials.getInstance()` |
| `applicationDefault()` | `GoogleCredentials.getApplicationDefault()` (env, gcloud, GCE/GKE metadata, …) |
| `serviceAccount(file)` | `ServiceAccountCredentials.fromStream(file)` |
| `serviceAccount(json)` | `ServiceAccountCredentials.fromStream(json.byteInputStream())` |
| `accessToken(token)` | `GoogleCredentials.create(AccessToken(token, null))` |

## `AbstractGcpClientBuildService<C, P>`

Abstract base class for Google Cloud client build services. Extend this and implement `createClient()`,
using `resolveCredentials()` (for builders that accept a `Credentials` directly, e.g.
`StorageOptions.Builder.setCredentials`) or `resolveCredentialsProvider()` (for builders that accept a
`gax` `CredentialsProvider`, e.g. `TopicAdminSettings`):

```kotlin
abstract class StorageClientBuildService : AbstractGcpClientBuildService<Storage, GcpBuildServiceParams>() {
    override fun createClient(): Storage = StorageOptions.newBuilder().apply {
        parameters.projectId.orNull?.let(::setProjectId)
        resolveCredentials()?.let(::setCredentials)
    }.build().service
}
```

```kotlin
abstract class TopicAdminClientBuildService : AbstractGcpClientBuildService<TopicAdminClient, GcpBuildServiceParams>() {
    override fun createClient(): TopicAdminClient {
        val settings = TopicAdminSettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return TopicAdminClient.create(settings)
    }
}
```

| Method | Description |
|---|---|
| `resolveCredentials(): Credentials?` | Constructs the credentials object from `credentialSource`. Returns `null` when `credentialSource` is unset. |
| `resolveCredentialsProvider(): CredentialsProvider?` | Wraps `resolveCredentials()` in a `FixedCredentialsProvider`, with `NoCredentialsProvider.create()` for `NONE` and `null` when unset. |

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [Google Cloud Java Client Libraries](https://cloud.google.com/java/docs/reference)
