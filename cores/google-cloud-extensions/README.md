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
| `credentialSource` | `Property<GcpCredentialSource>` | Which credentials object to construct. Leave unset for the SDK's default resolution. Use extension functions rather than setting this directly. |
| `credentialsFile` | `RegularFileProperty` | Service account JSON key file. Used with `SERVICE_ACCOUNT_JSON_FILE`. |
| `credentialsJsonRef` | `Property<CredentialReference>` | Reference to a service account JSON payload in an env var or system property. Used with `SERVICE_ACCOUNT_JSON_ENV`. |
| `accessTokenRef` | `Property<CredentialReference>` | Reference to a static OAuth2 access token in an env var or system property. Used with `ACCESS_TOKEN`. |
| `externalAccountConfigFile` | `RegularFileProperty` | Credential config JSON file produced by `gcloud iam workload-identity-pools create-cred-config`. Used with `EXTERNAL_ACCOUNT_CONFIG_FILE`. |
| `externalAccountConfigRef` | `Property<CredentialReference>` | Reference to a credential config JSON payload in an env var or system property. Used with `EXTERNAL_ACCOUNT_CONFIG_ENV`. |
| `workloadIdentityAudience` | `Property<String>` | Full pool provider resource name used as the STS audience. Used with `WORKLOAD_IDENTITY_OIDC`. |
| `workloadIdentityTokenRef` | `Property<CredentialReference>` | Reference to a pre-fetched OIDC token in an env var or system property. Used with `WORKLOAD_IDENTITY_OIDC`. |
| `workloadIdentityServiceAccountEmail` | `Property<String>` | Email of the service account to impersonate. Optional; used with `WORKLOAD_IDENTITY_OIDC`. |

### Extension functions

Configure a `GcpBuildServiceParams` instance atomically using one of these functions:

```kotlin
gradle.sharedServices.registerIfAbsent("storage", StorageClientBuildService::class) {
    parameters {
        projectId.set("my-project")
        applicationDefault()                                                             // GoogleCredentials.getApplicationDefault()
        // noCredentials()                                                               // NoCredentials.getInstance()
        // serviceAccount(layout.projectDirectory.file("service-account.json"))          // SA key from file
        // serviceAccount()                                                              // SA key from GOOGLE_APPLICATION_CREDENTIALS_JSON env var
        // accessToken()                                                                 // Static token from GOOGLE_OAUTH2_TOKEN env var
        // externalAccount(layout.projectDirectory.file("cred-config.json"))             // External account config from file
        // externalAccount()                                                             // External account config from GOOGLE_EXTERNAL_ACCOUNT_CONFIG env var
        // workloadIdentity("//iam.googleapis.com/...", CredentialReference.EnvironmentVariable("OIDC_TOKEN"))
        // workloadIdentity("//iam.googleapis.com/...", CredentialReference.EnvironmentVariable("OIDC_TOKEN"), impersonateServiceAccount = "sa@project.iam.gserviceaccount.com")
    }
}
```

| Function | Credential result |
|---|---|
| `noCredentials()` | `NoCredentials.getInstance()` |
| `applicationDefault()` | `GoogleCredentials.getApplicationDefault()` (env, gcloud, GCE/GKE metadata, …) |
| `serviceAccount(file)` | `ServiceAccountCredentials.fromStream(file)` |
| `serviceAccount(json)` | `ServiceAccountCredentials.fromStream(json.resolve().byteInputStream())` |
| `accessToken(token)` | `GoogleCredentials.create(AccessToken(token.resolve(), null))` |
| `externalAccount(file)` | `ExternalAccountCredentials.fromStream(file)` — supports impersonation encoded in the JSON |
| `externalAccount(json)` | `ExternalAccountCredentials.fromStream(json.resolve().byteInputStream())` — supports impersonation encoded in the JSON |
| `workloadIdentity(audience, token)` | `IdentityPoolCredentials` with `SubjectTokenSupplier` resolving `token` lazily at refresh time |
| `workloadIdentity(audience, token, impersonateServiceAccount)` | Same as above, with automatic service account impersonation via the IAM Credentials API |

#### Workload Identity Federation

`externalAccount(file)` and `externalAccount(json)` load a credential config JSON generated by
`gcloud iam workload-identity-pools create-cred-config`. This is the recommended approach for static
provider configurations (e.g. GitHub Actions, GitLab CI) and supports service account impersonation
when the JSON includes a `service_account_impersonation_url` field.

`workloadIdentity(audience, token, ...)` constructs `IdentityPoolCredentials` programmatically.
The OIDC token is pre-fetched externally (e.g. by a CI step) and made available via an env var
or system property. The `audience` is the full pool provider resource name:

```
//iam.googleapis.com/projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL_ID/providers/PROVIDER_ID
```

When `impersonateServiceAccount` is provided, the STS-exchanged token is automatically chained
through service account impersonation — the caller only needs to supply the SA email.

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
