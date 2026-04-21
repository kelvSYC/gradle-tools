package com.kelvsyc.gradle.google.cloud.storage

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.plugins.ClientsBasePlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Specialization of [AbstractBatchUploadToGCS] integrating it with the clients-base core.
 */
@DisableCachingByDefault(because = "Uploading to an external service is not cacheable")
abstract class BatchUploadToGCS @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    workerExecutor: WorkerExecutor
) : AbstractBatchUploadToGCS(objects, providers, workerExecutor) {
    @get:ServiceReference(ClientsBasePlugin.SERVICE_NAME)
    abstract override val service: Property<ClientsBaseService>

    /**
     * Registered name of a [StorageClientInfo]
     */
    @get:Internal
    abstract override val clientName: Property<String>
}
