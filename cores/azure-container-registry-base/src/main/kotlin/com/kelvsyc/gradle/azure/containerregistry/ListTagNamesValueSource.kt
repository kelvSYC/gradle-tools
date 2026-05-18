package com.kelvsyc.gradle.azure.containerregistry

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of all tag names for a specific repository
 * in an Azure Container Registry.
 *
 * Returns the names of all tags (e.g., "v1.0.0", "latest") for the repository provided via the repository service.
 * Tags are aggregated from all manifest properties in the repository. The result of `obtain()` is serialized to
 * the Gradle configuration cache. This ValueSource only returns non-sensitive string identifiers (tag names),
 * so it is safe to use at configuration time.
 */
abstract class ListTagNamesValueSource : ValueSource<List<String>, ListTagNamesValueSource.Parameters> {
    /**
     * Parameters for [ListTagNamesValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the repository-scoped [com.azure.containers.containerregistry.ContainerRepository] client.
         */
        @get:Internal
        val service: Property<ContainerRepositoryClientBuildService>
    }

    override fun obtain(): List<String> =
        parameters.service.get().getClient().listManifestProperties()
            .flatMap { it.tags ?: emptyList() }
            .toList()
}
