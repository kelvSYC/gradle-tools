package com.kelvsyc.gradle.aws.java.sns

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * A [AbstractPublishBatch] task wired to an [SnsClient][software.amazon.awssdk.services.sns.SnsClient] supplied
 * by a [SnsClientBuildService].
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class PublishBatch @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractPublishBatch(objects, providers) {
    /**
     * Build service managing the SNS client to use.
     */
    @get:ServiceReference
    abstract val service: Property<SnsClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
