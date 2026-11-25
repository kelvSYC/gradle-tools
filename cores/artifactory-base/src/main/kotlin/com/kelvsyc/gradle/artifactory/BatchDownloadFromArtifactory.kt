package com.kelvsyc.gradle.artifactory

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault

/**
 * Specialization of [AbstractBatchDownloadFromArtifactory] that adds the `@get:ServiceReference` annotation
 * to the `service` property so the build service is automatically tracked as a task dependency.
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class BatchDownloadFromArtifactory : AbstractBatchDownloadFromArtifactory() {
    @get:ServiceReference
    abstract override val service: Property<ArtifactoryClientBuildService>
}
