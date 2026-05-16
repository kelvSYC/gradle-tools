# HashiCorp Vault Extensions

Kotlin extension library for HashiCorp Vault integrations in Gradle build services. Provides type-safe configuration, automatic token renewal, and secure credential lease management.

## Dependency

```kotlin
dependencies {
    implementation("com.kelvsyc.gradle:hashicorp-vault-extensions:1.0.0")
}
```

## Design Note

**Configuration-time credential access is not supported.** All credential retrieval must occur at task execution time (inside `@TaskAction` or `WorkAction.execute()`). This is an intentional security design: credentials are fetched only when absolutely needed, and sensitive values never enter the Gradle configuration cache.

## Authentication Methods

| Method | Extension Function | Required Fields |
|--------|-------------------|-----------------|
| Pre-issued Token | `tokenAuth()` | Token reference (env var: `VAULT_TOKEN`) |
| AppRole | `appRoleAuth(roleId)` | Role ID, secret ID reference (env var: `VAULT_SECRET_ID`) |
| Kubernetes | `kubernetesAuth(role)` | Role name, JWT path (default: `/var/run/secrets/kubernetes.io/serviceaccount/token`) |
| AWS IAM | `awsIamAuth()` | None — uses ambient AWS credentials |
| Google Cloud | `gcpAuth()` | GCP JWT reference |
| Azure Managed Identity | `azureMsiAuth()` | Azure JWT reference |

## Build Service Registration

### Token Authentication

```kotlin
gradle.sharedServices.registerIfAbsent("vaultToken", MyVaultBuildService::class) {
    parameters {
        endpoint.set("https://vault.example.com:8200")
        tokenAuth()  // Uses VAULT_TOKEN env var
    }
}
```

### AppRole Authentication

```kotlin
gradle.sharedServices.registerIfAbsent("vaultAppRole", MyVaultBuildService::class) {
    parameters {
        endpoint.set("https://vault.example.com:8200")
        appRoleAuth(
            roleId = "my-role",
            secretId = CredentialReference.EnvironmentVariable("MY_SECRET_ID")
        )
    }
}
```

## Primary Usage Pattern

Define your own `WorkAction` with a `VaultClientBuildService` injected as a parameter:

```kotlin
abstract class FetchDatabaseCredentialsAction : WorkAction<FetchDatabaseCredentialsAction.Params> {
    interface Params : WorkParameters {
        val vault: Property<MyVaultBuildService>
        val role: Property<String>
        val outputFile: RegularFileProperty
    }

    override fun execute() {
        val vaultService = parameters.vault.get()
        val credential = vaultService.issueDatabaseCredential(parameters.role.get())
        
        // Use credential: pass to next tool, write to file, etc.
        parameters.outputFile.get().asFile.writeText(
            "USERNAME=${credential.username}\nPASSWORD=${credential.password}"
        )
        // Credential is NOT automatically revoked here — see withDatabaseCredential below.
    }
}
```

Register and use this action inside a task:

```kotlin
tasks.register<JavaExec>("fetchDbCreds") {
    val vault = gradle.sharedServices.registerIfAbsent("vault", MyVaultBuildService::class) {
        parameters {
            endpoint.set("https://vault.example.com:8200")
            tokenAuth()
        }
    }

    val workQueue = workerExecutor.classLoaderIsolation()
    doLast {
        workQueue.submit(FetchDatabaseCredentialsAction::class) {
            vault.set(vault)
            role.set("my-db-role")
            outputFile.set(layout.buildDirectory.file("secrets/db.env"))
        }
    }
}
```

## Scoped Credential Access

For simpler cases, use the `with*Credential` helpers for ergonomic credential issuance with immediate revocation:

```kotlin
abstract class MyVaultBuildService : AbstractVaultClientBuildService<MyVaultBuildService.Params>() {
    interface Params : VaultBuildServiceParams {
        // Add any custom fields here
    }
}

// Inside a task @TaskAction or WorkAction.execute():
val vaultService = // ... obtain the build service instance
vaultService.withDatabaseCredential("my-db-role") { credential ->
    // Use credential here
    println("Database user: ${credential.username}")
    // Lease is revoked in a try/finally immediately after this block
}
```

Equivalent with AWS credentials:

```kotlin
vaultService.withAwsCredential("my-aws-role") { credential ->
    val s3Client = AmazonS3ClientBuilder.standard()
        .withCredentials(AWSStaticCredentialsProvider(
            BasicAWSCredentials(credential.accessKeyId, credential.secretAccessKey)
        ))
        .build()
    // Use s3Client
    // Credential is revoked immediately after this block
}
```

## Lease Lifecycle

Dynamic credentials are revoked through a two-layer strategy:

### Layer 1: Immediate Revocation (Tight Window)

Methods like `withDatabaseCredential()` and `withAwsCredential()` revoke the lease in a `try/finally` block:

```kotlin
fun <T> withDatabaseCredential(role: String, block: (DatabaseCredential) -> T): T {
    val credential = issueDatabaseCredential(role)
    return try {
        block(credential)
    } finally {
        revokeLease(credential.leaseId)  // Revoked immediately
    }
}
```

If you call `issueDatabaseCredential()` directly (without the `with*` wrapper), the credential is issued but not immediately revoked — revocation defers to Layer 2.

### Layer 2: Safety-Net Revocation (Build Service Cleanup)

When the build service closes (at the end of the build or when garbage collected), the `close()` method revokes any leases still in the internal registry:

```kotlin
override fun close() {
    for (leaseId in leaseRegistry) {
        runCatching { getClient().leases().revoke(leaseId) }
            .onFailure { logger.warn("Failed to revoke Vault lease $leaseId during cleanup", it) }
    }
    super.close()
}
```

Revocation failures are logged as warnings and do not fail the build. This ensures that even if a task forgets to revoke a credential, Vault leases do not accumulate indefinitely.

## TLS Configuration

### Verify Using Default Trust Store

By default, the JVM's default certificate trust store is used:

```kotlin
parameters {
    endpoint.set("https://vault.example.com:8200")
    tokenAuth()
    // Default: uses JVM trust store
}
```

### Verify Using Custom CA Certificate

```kotlin
parameters {
    endpoint.set("https://vault.example.com:8200")
    tokenAuth()
    caCertFile.set(file("config/vault-ca.pem"))
}
```

### Disable Verification (Development Only)

```kotlin
parameters {
    endpoint.set("https://vault.example.com:8200")
    tokenAuth()
    skipVerify.set(true)
    // WARNING: Disables certificate verification; vulnerable to MITM attacks
    // Only use in controlled development environments with self-signed certs
}
```

## Vault Enterprise: Namespace Configuration

Vault Enterprise supports multi-tenancy via namespaces. Set the namespace if your Vault instance uses them:

```kotlin
parameters {
    endpoint.set("https://vault.example.com:8200")
    namespace.set("my-team")  // Vault Enterprise only
    tokenAuth()
}
```

For open-source Vault, omit this field — it defaults to the root namespace.
