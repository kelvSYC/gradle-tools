package com.kelvsyc.gradle.aws.kotlin.s3

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.plugins.ClientsBasePlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Internal
import javax.inject.Inject

abstract class BatchDownloadFromS3 @Inject constructor(objects: ObjectFactory) : AbstractBatchDownloadFromS3(objects) {
    @get:ServiceReference(ClientsBasePlugin.SERVICE_NAME)
    abstract val service: Property<ClientsBaseService>

    @get:Internal
    abstract val clientName: Property<String>
}
