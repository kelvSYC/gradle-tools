package com.kelvsyc.gradle.aws.kotlin.ses

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.plugins.ClientsBasePlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * A [AbstractSendBulkTemplatedMail] task wired to an [SesClient][aws.sdk.kotlin.services.ses.SesClient]
 * registered in [ClientsBaseService].
 */
@DisableCachingByDefault(because = "Sending email is not cacheable")
abstract class SendBulkTemplatedMail @Inject constructor(
    objects: ObjectFactory
) : AbstractSendBulkTemplatedMail(objects) {
    /**
     * The shared [ClientsBaseService] holding the registered SES client.
     */
    @get:ServiceReference(ClientsBasePlugin.EXTENSION_NAME)
    abstract val service: Property<ClientsBaseService>

    /**
     * Registered name of a [SesClientInfo].
     */
    @get:Internal
    abstract val clientName: Property<String>
}
