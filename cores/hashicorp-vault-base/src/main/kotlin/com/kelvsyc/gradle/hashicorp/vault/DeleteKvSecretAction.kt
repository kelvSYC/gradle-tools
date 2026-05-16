package com.kelvsyc.gradle.hashicorp.vault

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that deletes a Vault KV secrets engine path.
 *
 * Submit this action via [org.gradle.workers.WorkerExecutor.noIsolation]:
 *
 * ```kotlin
 * workerExecutor.noIsolation().submit(DeleteKvSecretAction::class) {
 *     service.set(vaultService)
 *     path.set("secret/data/myapp")
 * }
 * ```
 */
abstract class DeleteKvSecretAction : WorkAction<DeleteKvSecretAction.Parameters> {
    /** Parameters for [DeleteKvSecretAction]. */
    interface Parameters : WorkParameters {
        /**
         * The Vault build service used to perform the delete operation.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<VaultClientBuildService>

        /** The KV path to delete. */
        val path: Property<String>
    }

    override fun execute() {
        parameters.service.get().getClient().logical().delete(parameters.path.get())
    }
}
