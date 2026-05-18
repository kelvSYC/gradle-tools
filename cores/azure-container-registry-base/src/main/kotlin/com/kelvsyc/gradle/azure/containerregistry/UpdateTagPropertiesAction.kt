package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.models.ArtifactTagProperties
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that updates write/delete protection flags on a specific tag in Azure Container Registry
 * using a repository-scoped [ContainerRepository][com.azure.containers.containerregistry.ContainerRepository].
 *
 * The action configures the [ArtifactTagProperties] (writeEnabled, deleteEnabled) and applies them
 * atomically to the tag.
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when updating
 * multiple tags in a task action.
 */
abstract class UpdateTagPropertiesAction : WorkAction<UpdateTagPropertiesAction.Parameters> {
    /**
     * Parameters for [UpdateTagPropertiesAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the repository-scoped Container Repository client.
         */
        @get:Internal
        val service: Property<ContainerRepositoryClientBuildService>

        /**
         * The name of the tag to update, e.g., `v1.0.0` or `latest`.
         */
        val tagName: Property<String>

        /**
         * Whether the tag can be written to (push new versions). Set to false to prevent tag mutations.
         */
        val canWrite: Property<Boolean>

        /**
         * Whether the tag can be deleted. Set to false to prevent tag deletion.
         */
        val canDelete: Property<Boolean>
    }

    override fun execute() {
        val props = ArtifactTagProperties()
            .setWriteEnabled(parameters.canWrite.get())
            .setDeleteEnabled(parameters.canDelete.get())

        parameters.service.get().getClient()
            .getArtifact(parameters.tagName.get())
            .updateTagProperties(parameters.tagName.get(), props)
    }
}
