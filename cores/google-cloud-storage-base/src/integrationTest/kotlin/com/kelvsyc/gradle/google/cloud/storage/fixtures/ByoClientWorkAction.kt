package com.kelvsyc.gradle.google.cloud.storage.fixtures

import com.google.cloud.storage.Storage
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Variant B proposed BYO retrofit: a `WorkAction` whose `WorkParameters` directly holds the live SDK client
 * (`Property<Storage>`). The probe answers whether `Property<LiveClient>` survives `WorkerExecutor`
 * submission serialization for the GCS SDK.
 */
abstract class ByoClientWorkAction : WorkAction<ByoClientWorkAction.Parameters> {
    /** Parameters for [ByoClientWorkAction]. */
    interface Parameters : WorkParameters {
        /** The live SDK client under probe. */
        val client: Property<Storage>
    }

    override fun execute() {
        val client = checkNotNull(parameters.client.orNull) { "client property unexpectedly absent" }
        check(client::class.qualifiedName != null)
    }
}
