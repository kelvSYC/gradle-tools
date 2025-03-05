package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.plugins.ClientsBasePlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Internal
import javax.inject.Inject

abstract class BatchUploadToS3 @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractBatchUploadToS3(objects, providers) {
    @get:ServiceReference(ClientsBasePlugin.SERVICE_NAME)
    abstract val clientsService : Property<ClientsBaseService>

    /**
     * Registered name of a [S3TransferManagerClientInfo]
     */
    @get:Internal
    abstract val clientName: Property<String>
}
