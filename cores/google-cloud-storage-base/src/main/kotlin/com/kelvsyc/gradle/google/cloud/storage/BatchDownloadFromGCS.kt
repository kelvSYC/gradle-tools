package com.kelvsyc.gradle.google.cloud.storage

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.plugins.ClientsBasePlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Internal
import javax.inject.Inject

/**
 * Specialization of [AbstractBatchDownloadFromGCS] integrating it with the clients-base core.
 */
abstract class BatchDownloadFromGCS @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractBatchDownloadFromGCS(objects, providers) {
    @get:ServiceReference(ClientsBasePlugin.SERVICE_NAME)
    abstract val clientsService : Property<ClientsBaseService>

    /**
     * Registered name of a [StorageClientInfo]
     */
    @get:Internal
    abstract val clientName: Property<String>
}
