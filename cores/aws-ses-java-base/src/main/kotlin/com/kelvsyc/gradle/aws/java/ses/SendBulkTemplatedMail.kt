package com.kelvsyc.gradle.aws.java.ses

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault

/**
 * A [AbstractSendBulkTemplatedMail] task wired to an [SesClient][software.amazon.awssdk.services.ses.SesClient]
 * supplied by a [SesClientBuildService].
 */
@DisableCachingByDefault(because = "Sending email is not cacheable")
abstract class SendBulkTemplatedMail : AbstractSendBulkTemplatedMail() {
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
