package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault
import io.github.jopenlibs.vault.VaultConfig
import io.github.jopenlibs.vault.SslConfig
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.logging.Logging
import java.io.File
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Abstract base class for HashiCorp Vault build services.
 *
 * Manages the lifecycle of a [Vault] client, including:
 * - Authentication via the method configured in [VaultBuildServiceParams]
 * - Automatic token renewal at 80% of the token TTL
 * - Lease tracking and revocation for dynamic credentials
 *
 * ## Execution-time only
 *
 * Configuration-time credential access is explicitly not supported. All methods
 * that retrieve secrets or issue credentials ([getKvSecret], [getKvSecretMap],
 * [issueDatabaseCredential], etc.) must be called at task execution time — either
 * inside a [org.gradle.workers.WorkAction.execute] implementation or inside a
 * `@TaskAction`-annotated method.
 *
 * ## Lease revocation
 *
 * Dynamic credentials are managed through a two-layer revocation strategy:
 *
 * 1. **Immediate revocation** — The [withDatabaseCredential], [withAwsCredential],
 *    [withGcpCredential], and [withAzureCredential] helpers revoke the lease in a
 *    `try/finally` block immediately after the caller's block completes. The abstract
 *    WorkActions in `hashicorp-vault-base` follow the same pattern.
 *
 * 2. **Safety-net revocation** — All issued leases are tracked in an internal registry.
 *    On [close], any leases not yet revoked are revoked as a best-effort cleanup.
 *    Revocation failures are logged as warnings and do not fail the build.
 *
 * ## TLS
 *
 * TLS certificate verification is enabled by default. See [VaultBuildServiceParams.skipVerify]
 * for the option to disable it — but never do so in production.
 */
@Suppress("TooManyFunctions")
abstract class AbstractVaultClientBuildService<P : VaultBuildServiceParams> :
    AbstractClientBuildService<Vault, P>() {

    private val logger = Logging.getLogger(AbstractVaultClientBuildService::class.java)

    @Volatile
    private var clientInitialized = false

    private val leaseRegistry = CopyOnWriteArrayList<String>()
    private var renewalExecutor: ScheduledExecutorService? = null

    companion object {
        private const val TOKEN_RENEWAL_THRESHOLD = 0.8
    }

    override fun createClient(): Vault {
        clientInitialized = true

        val configBuilder = VaultConfig()
            .address(parameters.endpoint.get())

        parameters.namespace.orNull?.let { configBuilder.nameSpace(it) }

        if (parameters.skipVerify.getOrElse(false)) {
            configBuilder.sslConfig(
                SslConfig().verify(false).build()
            )
        } else {
            parameters.caCertFile.orNull?.let { file ->
                configBuilder.sslConfig(
                    SslConfig()
                        .pemFile(file.asFile)
                        .build()
                )
            }
        }

        val config = configBuilder.build()
        val vault = Vault.create(config)

        authenticate(vault)
        scheduleTokenRenewal(vault)

        return vault
    }

    private fun authenticate(vault: Vault) {
        when (parameters.credentialSource.get()) {
            VaultCredentialSource.TOKEN -> {
                val token = parameters.tokenRef.get().resolve()
                val configBuilder = VaultConfig()
                    .address(parameters.endpoint.get())
                    .token(token)
                parameters.namespace.orNull?.let { configBuilder.nameSpace(it) }
            }
            VaultCredentialSource.APP_ROLE -> {
                val roleId = parameters.roleId.get()
                val secretId = parameters.secretIdRef.get().resolve()
                vault.auth().loginByAppRole(roleId, secretId)
            }
            VaultCredentialSource.KUBERNETES -> {
                val role = parameters.kubernetesRole.get()
                val jwtPath = parameters.kubernetesJwtPath.get()
                val jwt = File(jwtPath).readText().trim()
                vault.auth().loginByKubernetes(role, jwt)
            }
            VaultCredentialSource.AWS_IAM -> {
                vault.auth().loginByAwsIam("aws", null, null, null, null)
            }
            VaultCredentialSource.GCP -> {
                val jwt = parameters.jwtRef.get().resolve()
                vault.auth().loginByGCP("gcp", jwt)
            }
            VaultCredentialSource.AZURE_MSI -> {
                error("Azure Managed Identity authentication is not supported by vault-java-driver")
            }
        }
    }

    private fun scheduleTokenRenewal(vault: Vault) {
        val ttlSeconds = runCatching {
            vault.auth().lookupSelf().ttl
        }.getOrElse {
            logger.warn("Could not determine token TTL for renewal scheduling; token renewal disabled", it)
            return
        }

        if (ttlSeconds <= 0) {
            logger.info("Vault token does not expire; token renewal not scheduled")
            return
        }

        val renewalIntervalSeconds = (ttlSeconds * TOKEN_RENEWAL_THRESHOLD).toLong()
        val executor = Executors.newSingleThreadScheduledExecutor()
        renewalExecutor = executor

        executor.scheduleAtFixedRate(
            {
                runCatching { vault.auth().renewSelf() }
                    .onFailure { logger.warn("Vault token renewal failed", it) }
            },
            renewalIntervalSeconds,
            renewalIntervalSeconds,
            TimeUnit.SECONDS,
        )
    }

    /**
     * Tracks a Vault lease for safety-net revocation on [close].
     * Called internally by all credential issuance methods.
     */
    fun trackLease(leaseId: String) {
        leaseRegistry.add(leaseId)
    }

    /**
     * Revokes a Vault lease and removes it from the internal registry.
     * Revocation failures are logged as warnings and do not throw.
     */
    @Suppress("DEPRECATION")
    fun revokeLease(leaseId: String) {
        runCatching { getClient().leases().revoke(leaseId) }
            .onFailure { logger.warn("Failed to revoke Vault lease $leaseId", it) }
        leaseRegistry.remove(leaseId)
    }

    /**
     * Retrieves a single value from a KV secrets engine path.
     *
     * Must be called at task execution time. Configuration-time access is not supported.
     *
     * @param path The KV path, e.g. `secret/data/myapp` (KV v2) or `secret/myapp` (KV v1).
     * @param key The key within the secret.
     * @return The secret value.
     * @throws IllegalStateException if the key does not exist at the given path.
     */
    fun getKvSecret(path: String, key: String): String =
        getKvSecretMap(path)[key]
            ?: error("Key '$key' not found at Vault path '$path'")

    /**
     * Retrieves all key-value pairs from a KV secrets engine path.
     *
     * Must be called at task execution time. Configuration-time access is not supported.
     *
     * @param path The KV path.
     * @return A map of all key-value pairs at the path.
     */
    fun getKvSecretMap(path: String): Map<String, String> =
        getClient().logical().read(path).data

    /**
     * Issues dynamic database credentials from Vault's database secrets engine.
     *
     * The issued lease is registered for safety-net revocation on [close]. For the
     * tightest revocation window, use [withDatabaseCredential] instead.
     *
     * @param role The database role configured in Vault.
     * @return The issued [DatabaseCredential].
     */
    fun issueDatabaseCredential(role: String): DatabaseCredential {
        val response = getClient().logical().read("database/creds/$role")
        val leaseId = response.leaseId
        trackLease(leaseId)
        return DatabaseCredential(
            username = response.data.getValue("username"),
            password = response.data.getValue("password"),
            leaseId = leaseId,
            leaseDuration = Duration.ofSeconds(response.leaseDuration),
        )
    }

    /**
     * Issues dynamic AWS credentials from Vault's AWS secrets engine.
     *
     * The issued lease is registered for safety-net revocation on [close]. For the
     * tightest revocation window, use [withAwsCredential] instead.
     *
     * @param role The AWS role configured in Vault.
     * @return The issued [AwsDynamicCredential].
     */
    fun issueAwsCredential(role: String): AwsDynamicCredential {
        val response = getClient().logical().read("aws/creds/$role")
        val leaseId = response.leaseId
        trackLease(leaseId)
        return AwsDynamicCredential(
            accessKeyId = response.data.getValue("access_key"),
            secretAccessKey = response.data.getValue("secret_key"),
            sessionToken = response.data["security_token"],
            leaseId = leaseId,
            leaseDuration = Duration.ofSeconds(response.leaseDuration),
        )
    }

    /**
     * Issues dynamic GCP credentials from Vault's GCP secrets engine.
     *
     * The issued lease is registered for safety-net revocation on [close]. For the
     * tightest revocation window, use [withGcpCredential] instead.
     *
     * @param role The GCP roleset configured in Vault.
     * @return The issued [GcpDynamicCredential].
     */
    fun issueGcpCredential(role: String): GcpDynamicCredential {
        val response = getClient().logical().read("gcp/token/$role")
        val leaseId = response.leaseId
        trackLease(leaseId)
        return GcpDynamicCredential(
            token = response.data.getValue("token"),
            leaseId = leaseId,
            leaseDuration = Duration.ofSeconds(response.leaseDuration),
        )
    }

    /**
     * Issues dynamic Azure credentials from Vault's Azure secrets engine.
     *
     * The issued lease is registered for safety-net revocation on [close]. For the
     * tightest revocation window, use [withAzureCredential] instead.
     *
     * @param role The Azure role configured in Vault.
     * @return The issued [AzureDynamicCredential].
     */
    fun issueAzureCredential(role: String): AzureDynamicCredential {
        val response = getClient().logical().read("azure/creds/$role")
        val leaseId = response.leaseId
        trackLease(leaseId)
        return AzureDynamicCredential(
            clientId = response.data.getValue("client_id"),
            clientSecret = response.data.getValue("client_secret"),
            leaseId = leaseId,
            leaseDuration = Duration.ofSeconds(response.leaseDuration),
        )
    }

    /**
     * Issues database credentials, executes [block] with them, then revokes the lease
     * immediately in a `try/finally` block.
     *
     * This provides the tightest possible revocation window. The BuildService lease
     * registry also tracks the lease as a safety net in case revocation fails.
     *
     * @param role The database role configured in Vault.
     * @param block The block to execute with the issued credential.
     * @return The return value of [block].
     */
    fun <T> withDatabaseCredential(role: String, block: (DatabaseCredential) -> T): T {
        val credential = issueDatabaseCredential(role)
        return try {
            block(credential)
        } finally {
            revokeLease(credential.leaseId)
        }
    }

    /**
     * Issues AWS credentials, executes [block] with them, then revokes the lease
     * immediately in a `try/finally` block.
     *
     * @param role The AWS role configured in Vault.
     * @param block The block to execute with the issued credential.
     * @return The return value of [block].
     */
    fun <T> withAwsCredential(role: String, block: (AwsDynamicCredential) -> T): T {
        val credential = issueAwsCredential(role)
        return try {
            block(credential)
        } finally {
            revokeLease(credential.leaseId)
        }
    }

    /**
     * Issues GCP credentials, executes [block] with them, then revokes the lease
     * immediately in a `try/finally` block.
     *
     * @param role The GCP roleset configured in Vault.
     * @param block The block to execute with the issued credential.
     * @return The return value of [block].
     */
    fun <T> withGcpCredential(role: String, block: (GcpDynamicCredential) -> T): T {
        val credential = issueGcpCredential(role)
        return try {
            block(credential)
        } finally {
            revokeLease(credential.leaseId)
        }
    }

    /**
     * Issues Azure credentials, executes [block] with them, then revokes the lease
     * immediately in a `try/finally` block.
     *
     * @param role The Azure role configured in Vault.
     * @param block The block to execute with the issued credential.
     * @return The return value of [block].
     */
    fun <T> withAzureCredential(role: String, block: (AzureDynamicCredential) -> T): T {
        val credential = issueAzureCredential(role)
        return try {
            block(credential)
        } finally {
            revokeLease(credential.leaseId)
        }
    }

    /**
     * Cancels token renewal, revokes all tracked leases, and closes the Vault client.
     *
     * Lease revocation is best-effort — individual failures are logged as warnings
     * and do not prevent cleanup of remaining leases or the client.
     */
    @Suppress("DEPRECATION")
    override fun close() {
        if (clientInitialized) {
            renewalExecutor?.shutdownNow()
            for (leaseId in leaseRegistry) {
                runCatching { getClient().leases().revoke(leaseId) }
                    .onFailure { logger.warn("Failed to revoke Vault lease $leaseId during cleanup", it) }
            }
        }
        super.close()
    }
}

