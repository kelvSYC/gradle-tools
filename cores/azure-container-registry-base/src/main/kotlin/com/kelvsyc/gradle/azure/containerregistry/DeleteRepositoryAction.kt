package com.kelvsyc.gradle.azure.containerregistry

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that deletes an entire repository from Azure Container Registry using a
 * registry-scoped [ContainerRegistryClient][com.azure.containers.containerregistry.ContainerRegistryClient].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when deleting
 * multiple repositories in a task action.
 */
abstract class DeleteRepositoryAction : WorkAction<DeleteRepositoryAction.Parameters> {
    /**
     * Parameters for [DeleteRepositoryAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the registry-scoped Container Registry client. */
        @get:Internal
        val service: Property<ContainerRegistryClientBuildService>

        /** The name of the repository to delete, e.g., `myrepo` or `myorg/myrepo`. */
        val repositoryName: Property<String>
    }

    override fun execute() {
        parameters.service.get().getClient().deleteRepository(parameters.repositoryName.get())
    }
}
