package com.kelvsyc.gradle.artifactory

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault

/**
 * Specialization of [AbstractBatchUploadToArtifactory] that adds the `@get:ServiceReference` annotation
 * to the `service` property so the build service is automatically tracked as a task dependency.
 */
@DisableCachingByDefault(because = "Uploading to an external service is not cacheable")
abstract class BatchUploadToArtifactory : AbstractBatchUploadToArtifactory() {
    @get:ServiceReference
    abstract override val service: Property<ArtifactoryClientBuildService>
}
