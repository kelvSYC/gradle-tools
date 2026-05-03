package com.kelvsyc.gradle.aws.java.sqs

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.plugins.ClientsBasePlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * A [AbstractSendMessageBatch] task wired to an [SqsClient][software.amazon.awssdk.services.sqs.SqsClient]
 * registered in [ClientsBaseService].
 */
@DisableCachingByDefault(because = "Sending to an external service is not cacheable")
abstract class SendMessageBatch @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractSendMessageBatch(objects, providers) {
    /**
     * The shared [ClientsBaseService] holding the registered SQS client.
     */
    @get:ServiceReference(ClientsBasePlugin.SERVICE_NAME)
    abstract val clientsService: Property<ClientsBaseService>

    /**
     * Registered name of an [SqsClientInfo].
     */
    @get:Internal
    abstract val clientName: Property<String>
}
