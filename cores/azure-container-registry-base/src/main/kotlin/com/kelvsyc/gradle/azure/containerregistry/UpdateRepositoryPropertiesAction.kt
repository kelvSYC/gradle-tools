package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.models.ContainerRepositoryProperties
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that updates write/delete/list protection flags on a repository in Azure
 * Container Registry using a repository-scoped [ContainerRepository][com.azure.containers.containerregistry.ContainerRepository].
 *
 * The action configures the [ContainerRepositoryProperties] (writeEnabled, deleteEnabled, listEnabled) and
 * applies them atomically to the repository.
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when updating
 * multiple repositories in a task action.
 */
abstract class UpdateRepositoryPropertiesAction : WorkAction<UpdateRepositoryPropertiesAction.Parameters> {
    /**
     * Parameters for [UpdateRepositoryPropertiesAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the repository-scoped Container Repository client. */
        @get:Internal
        val service: Property<ContainerRepositoryClientBuildService>

        /** Whether the repository can be written to (push new images). */
        val canWrite: Property<Boolean>

        /** Whether the repository can be deleted (delete images/manifests). */
        val canDelete: Property<Boolean>

        /** Whether the repository can be listed (list tags/manifests). */
        val canList: Property<Boolean>
    }

    override fun execute() {
        val props = ContainerRepositoryProperties()
            .setWriteEnabled(parameters.canWrite.get())
            .setDeleteEnabled(parameters.canDelete.get())
            .setListEnabled(parameters.canList.get())

        parameters.service.get().getClient().updateProperties(props)
    }
}
