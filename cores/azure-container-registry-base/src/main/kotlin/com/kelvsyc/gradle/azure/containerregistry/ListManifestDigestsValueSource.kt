package com.kelvsyc.gradle.azure.containerregistry

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of all manifest digests for a specific repository
 * in an Azure Container Registry.
 *
 * Returns the digests (SHA256 hashes) of all manifests for the repository provided via the repository service.
 * The result of `obtain()` is serialized to the Gradle configuration cache. This ValueSource only returns
 * non-sensitive string identifiers (manifest digests), so it is safe to use at configuration time.
 */
abstract class ListManifestDigestsValueSource : ValueSource<List<String>, ListManifestDigestsValueSource.Parameters> {
    /**
     * Parameters for [ListManifestDigestsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the repository-scoped [com.azure.containers.containerregistry.ContainerRepository] client.
         */
        @get:Internal
        val service: Property<ContainerRepositoryClientBuildService>
    }

    override fun obtain(): List<String> =
        parameters.service.get().getClient().listManifestProperties().map { it.digest }.toList()
}
