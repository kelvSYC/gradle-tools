package com.kelvsyc.gradle.aws.kotlin.s3

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.plugins.ClientsBasePlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Internal
import javax.inject.Inject

abstract class BatchUploadToS3 @Inject constructor(objects: ObjectFactory) : AbstractBatchUploadToS3(objects) {
    @get:ServiceReference(ClientsBasePlugin.EXTENSION_NAME)
    abstract val service: Property<ClientsBaseService>

    @get:Internal
    abstract val clientName: Property<String>
}
