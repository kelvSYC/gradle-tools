package com.kelvsyc.gradle.nexus

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault

/**
 * Specialization of [AbstractBatchUploadToNexus] that adds the `@get:ServiceReference`
 * annotation to the [service] property so Gradle automatically tracks the build service as a
 * task dependency.
 */
@DisableCachingByDefault(because = "Uploading to an external service is not cacheable")
abstract class BatchUploadToNexus : AbstractBatchUploadToNexus() {

    @get:ServiceReference
    abstract override val service: Property<NexusClientBuildService>
}
