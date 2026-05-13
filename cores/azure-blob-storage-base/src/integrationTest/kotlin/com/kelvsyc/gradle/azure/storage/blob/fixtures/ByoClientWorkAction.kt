package com.kelvsyc.gradle.azure.storage.blob.fixtures

import com.azure.storage.blob.BlobServiceClient
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Variant B proposed BYO retrofit: a `WorkAction` whose `WorkParameters` directly holds the live SDK
 * client (`Property<BlobServiceClient>`). The probe answers whether `Property<LiveClient>` survives
 * `WorkerExecutor` submission serialization for the Azure SDK.
 */
abstract class ByoClientWorkAction : WorkAction<ByoClientWorkAction.Parameters> {
    /** Parameters for [ByoClientWorkAction]. */
    interface Parameters : WorkParameters {
        /** The live SDK client under probe. */
        val client: Property<BlobServiceClient>
    }

    override fun execute() {
        val client = checkNotNull(parameters.client.orNull) { "client property unexpectedly absent" }
        check(client::class.qualifiedName != null)
    }
}
