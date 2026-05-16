package com.kelvsyc.gradle.hashicorp.vault

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that writes a single key-value pair to a Vault KV secrets engine path.
 *
 * Submit this action via [org.gradle.workers.WorkerExecutor.noIsolation]:
 *
 * ```kotlin
 * workerExecutor.noIsolation().submit(WriteKvSecretAction::class) {
 *     service.set(vaultService)
 *     path.set("secret/data/myapp")
 *     key.set("apiKey")
 *     value.set("my-secret-value")
 * }
 * ```
 */
abstract class WriteKvSecretAction : WorkAction<WriteKvSecretAction.Parameters> {
    /** Parameters for [WriteKvSecretAction]. */
    interface Parameters : WorkParameters {
        /**
         * The Vault build service used to perform the write operation.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<VaultClientBuildService>

        /** The KV path to write to, e.g. `secret/data/myapp` (KV v2) or `secret/myapp` (KV v1). */
        val path: Property<String>

        /** The key within the secret to write. */
        val key: Property<String>

        /**
         * The secret value to write.
         *
         * This field is marked `@get:Internal` because it contains sensitive data.
         * It does not contribute to task snapshots.
         */
        @get:Internal
        val value: Property<String>
    }

    override fun execute() {
        parameters.service.get().getClient().logical()
            .write(parameters.path.get(), mapOf(parameters.key.get() to parameters.value.get()))
    }
}
