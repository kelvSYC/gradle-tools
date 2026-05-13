package com.kelvsyc.gradle.aws.kotlin.sns

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * A [AbstractPublishBatch] task wired to an [SnsClient][aws.sdk.kotlin.services.sns.SnsClient]
 * managed by an [SnsClientBuildService].
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class PublishBatch @Inject constructor(
    objects: ObjectFactory
) : AbstractPublishBatch(objects) {
    /**
     * The shared build service managing the SNS client.
     */
    @get:ServiceReference
    abstract val service: Property<SnsClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
