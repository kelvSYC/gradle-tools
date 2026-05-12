package com.kelvsyc.gradle.aws.kotlin.s3

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class BatchDownloadFromS3 @Inject constructor(objects: ObjectFactory) : AbstractBatchDownloadFromS3(objects) {
    /**
     * The shared build service managing the S3 client.
     */
    @get:ServiceReference
    abstract val service: Property<S3ClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
