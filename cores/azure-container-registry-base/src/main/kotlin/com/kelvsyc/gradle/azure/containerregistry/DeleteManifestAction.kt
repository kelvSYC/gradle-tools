package com.kelvsyc.gradle.azure.containerregistry

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that deletes a manifest by digest from an Azure Container Registry
 * using a repository-scoped [ContainerRepository][com.azure.containers.containerregistry.ContainerRepository].
 *
 * The action deletes the manifest and all associated tags that reference it atomically.
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when deleting
 * multiple manifests in a task action.
 */
abstract class DeleteManifestAction : WorkAction<DeleteManifestAction.Parameters> {
    /**
     * Parameters for [DeleteManifestAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the repository-scoped Container Repository client.
         */
        @get:Internal
        val service: Property<ContainerRepositoryClientBuildService>

        /**
         * The digest of the manifest to delete, e.g., `sha256:abc123def456...`. This is a unique
         * identifier for the manifest content.
         */
        val digest: Property<String>
    }

    override fun execute() {
        parameters.service.get().getClient()
            .getArtifact(parameters.digest.get())
            .delete()
    }
}
