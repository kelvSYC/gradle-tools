package com.kelvsyc.gradle.azure.containerregistry

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of all repository names in an Azure Container Registry.
 *
 * Returns the names of all repositories accessible via the registry service.
 * The result of `obtain()` is serialized to the Gradle configuration cache. This ValueSource only returns
 * non-sensitive string identifiers (repository names), so it is safe to use at configuration time.
 */
abstract class ListRepositoryNamesValueSource : ValueSource<List<String>, ListRepositoryNamesValueSource.Parameters> {
    /**
     * Parameters for [ListRepositoryNamesValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the registry-scoped [com.azure.containers.containerregistry.ContainerRegistryClient].
         */
        @get:Internal
        val service: Property<ContainerRegistryClientBuildService>
    }

    override fun obtain(): List<String> =
        parameters.service.get().getClient().listRepositoryNames().toList()
}
