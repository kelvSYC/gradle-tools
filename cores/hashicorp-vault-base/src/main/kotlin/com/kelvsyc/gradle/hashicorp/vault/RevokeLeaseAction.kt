package com.kelvsyc.gradle.hashicorp.vault

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that explicitly revokes a Vault lease.
 *
 * Use this when you need to revoke a lease at a specific point in the task graph,
 * beyond the automatic revocation provided by [AbstractDatabaseCredentialWorkAction]
 * and its sibling classes. The [VaultClientBuildService] always revokes all
 * tracked leases on build completion as a safety net.
 *
 * ```kotlin
 * workerExecutor.noIsolation().submit(RevokeLeaseAction::class) {
 *     service.set(vaultService)
 *     leaseId.set(credential.leaseId)
 * }
 * ```
 */
abstract class RevokeLeaseAction : WorkAction<RevokeLeaseAction.Parameters> {
    /** Parameters for [RevokeLeaseAction]. */
    interface Parameters : WorkParameters {
        /**
         * The Vault build service used to revoke the lease.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<VaultClientBuildService>

        /**
         * The Vault lease ID to revoke.
         * Excluded from task snapshots as it is a sensitive runtime value.
         */
        @get:Internal
        val leaseId: Property<String>
    }

    override fun execute() {
        parameters.service.get().revokeLease(parameters.leaseId.get())
    }
}
