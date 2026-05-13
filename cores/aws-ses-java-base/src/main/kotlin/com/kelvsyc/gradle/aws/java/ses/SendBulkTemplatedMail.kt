package com.kelvsyc.gradle.aws.java.ses

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * A [AbstractSendBulkTemplatedMail] task wired to an [SesClient][software.amazon.awssdk.services.ses.SesClient]
 * supplied by a [SesClientBuildService].
 */
@DisableCachingByDefault(because = "Sending email is not cacheable")
abstract class SendBulkTemplatedMail @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractSendBulkTemplatedMail(objects, providers) {
    /**
     * Build service managing the SES client to use.
     */
    @get:ServiceReference
    abstract val service: Property<SesClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
