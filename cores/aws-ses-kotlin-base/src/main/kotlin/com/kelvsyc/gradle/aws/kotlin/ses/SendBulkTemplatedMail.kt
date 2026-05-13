package com.kelvsyc.gradle.aws.kotlin.ses

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * A [AbstractSendBulkTemplatedMail] task wired to an [SesClient][aws.sdk.kotlin.services.ses.SesClient]
 * managed by an [SesClientBuildService].
 */
@DisableCachingByDefault(because = "Sending email is not cacheable")
abstract class SendBulkTemplatedMail @Inject constructor(
    objects: ObjectFactory
) : AbstractSendBulkTemplatedMail(objects) {
    /**
     * The shared build service managing the SES client.
     */
    @get:ServiceReference
    abstract val service: Property<SesClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
