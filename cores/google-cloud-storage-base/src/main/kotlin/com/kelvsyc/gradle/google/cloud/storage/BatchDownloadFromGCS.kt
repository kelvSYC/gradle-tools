package com.kelvsyc.gradle.google.cloud.storage

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault

/**
 * Specialization of [AbstractBatchDownloadFromGCS] that obtains its [Storage][com.google.cloud.storage.Storage]
 * client from a [StorageClientBuildService].
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class BatchDownloadFromGCS : AbstractBatchDownloadFromGCS() {
    /**
     * Build service managing the GCS client to use.
     */
    @get:ServiceReference
    abstract val service: Property<StorageClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
