# HashiCorp Vault Base

A Kotlin library providing [HashiCorp Vault](https://www.vaultproject.io/) integration for Gradle builds. Includes WorkAction implementations for KV secrets, dynamic credentials, and lease management, built on `clients-base` and `hashicorp-vault-extensions`.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:hashicorp-vault-base")
}
```

## Setup

See [hashicorp-vault-extensions](../hashicorp-vault-extensions) for authentication setup and `VaultBuildServiceParams` configuration options (token, AppRole, Kubernetes, AWS IAM, GCP, and TLS).

## VaultClientBuildService Registration

Register the build service in your plugin or settings plugin:

```kotlin
val vault = gradle.sharedServices.registerIfAbsent("vault", VaultClientBuildService::class) {
    parameters {
        endpoint.set("https://vault.example.com:8200")
        tokenAuth()  // or appRoleAuth(), kubernetesAuth(), etc.
    }
}
```

## Primary Usage: Inject Service into Custom WorkActions

The recommended pattern is to inject `VaultClientBuildService` directly into your own custom [WorkAction](https://docs.gradle.org/current/javadoc/org/gradle/workers/WorkAction.html):

```kotlin
abstract class FetchSecretAction : WorkAction<FetchSecretAction.Parameters> {
    interface Parameters : WorkParameters {
        @get:Internal
        val vaultService: Property<VaultClientBuildService>
        val secretPath: Property<String>
    }

    override fun execute() {
        val secret = parameters.vaultService.get().getKvSecret(
            parameters.secretPath.get(),
            "apiKey"
        )
        println("Secret: $secret")
    }
}
```

Submit via `WorkerExecutor`:

```kotlin
workerExecutor.noIsolation().submit(FetchSecretAction::class) {
    vaultService.set(vault)
    secretPath.set("secret/data/myapp")
}
```

## KV WorkActions

WorkActions for reading and writing Vault KV secrets:

| Class | Purpose | Parameters |
|-------|---------|------------|
| `WriteKvSecretAction` | Write a key-value pair to a KV path | `service`, `path`, `key`, `value` |
| `DeleteKvSecretAction` | Delete a KV path | `service`, `path` |

### `WriteKvSecretAction`

Writes a single key to a KV secrets engine path. The `service` and `value` parameters are marked `@get:Internal` for task caching.

| Parameter | Type | Snapshot | Description |
|-----------|------|----------|-------------|
| `service` | `Property<VaultClientBuildService>` | Excluded | Vault build service |
| `path` | `Property<String>` | Included | KV path, e.g. `secret/data/myapp` (KV v2) |
| `key` | `Property<String>` | Included | Key name within the secret |
| `value` | `Property<String>` | Excluded | Secret value to write |

```kotlin
workerExecutor.noIsolation().submit(WriteKvSecretAction::class) {
    service.set(vault)
    path.set("secret/data/myapp")
    key.set("apiKey")
    value.set("secret-value-here")
}
```

### `DeleteKvSecretAction`

Deletes a KV path. The `service` parameter is marked `@get:Internal`.

| Parameter | Type | Snapshot | Description |
|-----------|------|----------|-------------|
| `service` | `Property<VaultClientBuildService>` | Excluded | Vault build service |
| `path` | `Property<String>` | Included | KV path to delete |

```kotlin
workerExecutor.noIsolation().submit(DeleteKvSecretAction::class) {
    service.set(vault)
    path.set("secret/data/myapp")
}
```

## Dynamic Credential WorkActions

WorkActions for issuing and revoking dynamic credentials. All follow the same two-layer revocation pattern (immediate try/finally + safety-net).

| Class | Credential Type | Issue Method | Usage |
|-------|-----------------|--------------|-------|
| `AbstractDatabaseCredentialWorkAction` | Database (username/password) | `issueDatabaseCredential(role)` | Abstract; subclass and implement `doExecute` |
| `AbstractAwsCredentialWorkAction` | AWS (access key + secret) | `issueAwsCredential(role)` | Abstract; subclass and implement `doExecute` |
| `AbstractGcpCredentialWorkAction` | GCP (access token) | `issueGcpCredential(role)` | Abstract; subclass and implement `doExecute` |
| `AbstractAzureCredentialWorkAction` | Azure (client ID + secret) | `issueAzureCredential(role)` | Abstract; subclass and implement `doExecute` |

### Subclassing Pattern

Extend one of the abstract classes and implement `doExecute(credential)` to use the issued credential:

```kotlin
abstract class RunDatabaseMigrationAction : AbstractDatabaseCredentialWorkAction() {
    override fun doExecute(credential: DatabaseCredential) {
        val conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost/mydb",
            credential.username,
            credential.password
        )
        conn.use { /* run migration */ }
    }
}
```

Submit via `WorkerExecutor`:

```kotlin
workerExecutor.noIsolation().submit(RunDatabaseMigrationAction::class) {
    service.set(vault)
    role.set("my-db-role")
}
```

### Shorthand: `with*Credential` Helpers

As an alternative to subclassing, call the `with*` methods on `VaultClientBuildService` directly (these are available at execution time only):

```kotlin
vault.get().withDatabaseCredential("my-db-role") { credential ->
    // use credential.username, credential.password
}
```

### Parameter Details

All abstract credential WorkActions expose these parameters:

| Parameter | Type | Snapshot | Description |
|-----------|------|----------|-------------|
| `service` | `Property<VaultClientBuildService>` | Excluded | Vault build service |
| `role` | `Property<String>` | Included | Role/roleset name (e.g. `my-db-role`) |

**Note:** The `role` parameter is intentionally included in task snapshots because role names are not credentials — they are configuration. In environments where role names are sensitive, mark the parameter `@get:Internal` in your subclass:

```kotlin
interface Parameters : AbstractDatabaseCredentialWorkAction.Parameters {
    @get:Internal
    override val role: Property<String>
}
```

## Lease Revocation Strategy

### Two-Layer Revocation

Dynamic credentials are revoked through two independent mechanisms:

1. **Immediate revocation** — All abstract credential WorkActions revoke the lease in a `try/finally` block immediately after `doExecute()` returns, guaranteeing revocation even if the work throws an exception.

2. **Safety-net revocation** — The `VaultClientBuildService` maintains an internal lease registry. On build completion (`close()` hook), any leases not yet revoked are revoked as best-effort cleanup. Failures are logged as warnings and do not fail the build.

This ensures that even if immediate revocation is missed (e.g., due to a WorkAction crash or process termination), Vault will revoke the lease when the build service shuts down.

### Explicit Revocation: `RevokeLeaseAction`

For fine-grained control, use `RevokeLeaseAction` to revoke a specific lease at a particular point in the task graph:

```kotlin
workerExecutor.noIsolation().submit(RevokeLeaseAction::class) {
    service.set(vault)
    leaseId.set(credential.leaseId)
}
```

This is useful when you want to revoke a credential before the abstract WorkAction's automatic revocation would occur, or when managing leases outside the abstract classes.

## Azure Credential Note

The `AbstractAzureCredentialWorkAction` is defined for completeness. However, Azure Managed Identity authentication (`VaultCredentialSource.AZURE_MSI`) is **not currently supported** by vault-java-driver 6.2.1. Using `AZURE_MSI` will throw an `UnsupportedOperationException` when the build service is created.

Azure service principal credentials can still be issued via the Azure secrets engine and used with `AbstractAzureCredentialWorkAction`.

## See Also

- [clients-base](../clients-base) — The underlying service client infrastructure
- [hashicorp-vault-extensions](../hashicorp-vault-extensions) — Authentication, credential types, and build service parameters
