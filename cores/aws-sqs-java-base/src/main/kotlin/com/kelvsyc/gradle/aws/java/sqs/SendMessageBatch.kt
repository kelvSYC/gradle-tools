package com.kelvsyc.gradle.aws.java.sqs

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * A [AbstractSendMessageBatch] task wired to an [SqsClient][software.amazon.awssdk.services.sqs.SqsClient]
 * supplied by a [SqsClientBuildService].
 */
@DisableCachingByDefault(because = "Sending to an external service is not cacheable")
abstract class SendMessageBatch @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractSendMessageBatch(objects, providers) {
    /**
     * Build service managing the SQS client to use.
     */
    @get:ServiceReference
    abstract val service: Property<SqsClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
