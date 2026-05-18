package com.kelvsyc.gradle.azure.containerregistry

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that deletes a specific tag from a repository in Azure Container Registry
 * using a repository-scoped [ContainerRepository][com.azure.containers.containerregistry.ContainerRepository].
 *
 * The action deletes the tag while preserving the manifest and any other tags that reference it.
 * If the tag is the last reference to a manifest, the manifest may be garbage-collected by the registry.
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when deleting
 * multiple tags in a task action.
 */
abstract class DeleteTagAction : WorkAction<DeleteTagAction.Parameters> {
    /**
     * Parameters for [DeleteTagAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the repository-scoped Container Repository client.
         */
        @get:Internal
        val service: Property<ContainerRepositoryClientBuildService>

        /**
         * The name of the tag to delete, e.g., `v1.0.0` or `latest`.
         */
        val tagName: Property<String>
    }

    override fun execute() {
        parameters.service.get().getClient()
            .getArtifact(parameters.tagName.get())
            .deleteTag(parameters.tagName.get())
    }
}
