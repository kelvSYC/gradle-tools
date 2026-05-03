package com.kelvsyc.gradle.aws.java.sns

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
 * A [AbstractPublishBatch] task wired to an [SnsClient][software.amazon.awssdk.services.sns.SnsClient]
 * registered in [ClientsBaseService].
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class PublishBatch @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractPublishBatch(objects, providers) {
    /**
     * The shared [ClientsBaseService] holding the registered SNS client.
     */
    @get:ServiceReference(ClientsBasePlugin.SERVICE_NAME)
    abstract val clientsService: Property<ClientsBaseService>

    /**
     * Registered name of an [SnsClientInfo].
     */
    @get:Internal
    abstract val clientName: Property<String>
}
