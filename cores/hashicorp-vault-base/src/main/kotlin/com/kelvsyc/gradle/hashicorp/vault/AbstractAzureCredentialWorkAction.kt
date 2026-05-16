package com.kelvsyc.gradle.hashicorp.vault

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Abstract base [WorkAction] that issues dynamic Azure credentials from Vault,
 * executes work with them, and immediately revokes the credential lease.
 *
 * ## Lease revocation
 *
 * This action uses a two-layer revocation strategy:
 *
 * 1. **Immediate revocation** — The lease is revoked in a `try/finally` block
 *    immediately after [doExecute] returns, whether or not it throws.
 * 2. **Safety-net revocation** — The lease is also tracked by the [VaultClientBuildService]
 *    and will be revoked on build completion if the immediate revocation fails.
 *
 * Subclass this action and implement [doExecute] to use the issued credentials:
 *
 * ```kotlin
 * abstract class AuthenticateApplicationAction : AbstractAzureCredentialWorkAction() {
 *     override fun doExecute(credential: AzureDynamicCredential) {
 *         // use credential.clientId, credential.clientSecret
 *     }
 * }
 * ```
 *
 * ## Azure Managed Identity note
 *
 * The Azure credential WorkAction is defined for completeness. However, Azure Managed Identity
 * authentication (`VaultCredentialSource.AZURE_MSI`) is not currently supported by
 * vault-java-driver 6.2.1. Using `AZURE_MSI` will throw an `UnsupportedOperationException`
 * when the build service is created.
 *
 * ## Role sensitivity
 *
 * The [Parameters.role] property is not marked `@get:Internal` because the role name
 * is not a credential value — it contributes to Gradle's up-to-date checks. In
 * environments where role names are considered sensitive, mark the property
 * `@get:Internal` in the subclass parameters override.
 */
abstract class AbstractAzureCredentialWorkAction :
    WorkAction<AbstractAzureCredentialWorkAction.Parameters> {

    /** Parameters for [AbstractAzureCredentialWorkAction]. */
    interface Parameters : WorkParameters {
        /**
         * The Vault build service used to issue and revoke credentials.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<VaultClientBuildService>

        /**
         * The Azure role configured in Vault. Not a credential value; contributes
         * to Gradle's up-to-date checks. Mark `@get:Internal` in subclasses if the
         * role name is considered sensitive in your environment.
         */
        val role: Property<String>
    }

    final override fun execute() {
        val credential = parameters.service.get().issueAzureCredential(parameters.role.get())
        try {
            doExecute(credential)
        } finally {
            parameters.service.get().revokeLease(credential.leaseId)
        }
    }

    /**
     * Executes work using the issued [credential].
     *
     * Called within a `try/finally` block — the credential lease is revoked after
     * this method returns, whether or not it throws.
     *
     * @param credential The issued [AzureDynamicCredential] containing client ID,
     *   client secret, lease ID, and lease duration.
     */
    protected abstract fun doExecute(credential: AzureDynamicCredential)
}
