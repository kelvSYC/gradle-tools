package com.kelvsyc.gradle.aws.kotlin.sqs

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * A [AbstractSendMessageBatch] task wired to an [SqsClient][aws.sdk.kotlin.services.sqs.SqsClient]
 * managed by an [SqsClientBuildService].
 */
@DisableCachingByDefault(because = "Sending to an external service is not cacheable")
abstract class SendMessageBatch @Inject constructor(
    objects: ObjectFactory
) : AbstractSendMessageBatch(objects) {
    /**
     * The shared build service managing the SQS client.
     */
    @get:ServiceReference
    abstract val service: Property<SqsClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
