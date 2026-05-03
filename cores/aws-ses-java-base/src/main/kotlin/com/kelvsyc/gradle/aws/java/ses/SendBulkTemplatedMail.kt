package com.kelvsyc.gradle.aws.java.ses

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
 * A [AbstractSendBulkTemplatedMail] task wired to an [SesClient][software.amazon.awssdk.services.ses.SesClient]
 * registered in [ClientsBaseService].
 */
@DisableCachingByDefault(because = "Sending email is not cacheable")
abstract class SendBulkTemplatedMail @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractSendBulkTemplatedMail(objects, providers) {
    /**
     * The shared [ClientsBaseService] holding the registered SES client.
     */
    @get:ServiceReference(ClientsBasePlugin.SERVICE_NAME)
    abstract val clientsService: Property<ClientsBaseService>

    /**
     * Registered name of a [SesClientInfo].
     */
    @get:Internal
    abstract val clientName: Property<String>
}
