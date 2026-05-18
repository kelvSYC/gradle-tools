package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.models.ArtifactManifestProperties
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that updates write/delete/list/read protection flags on a specific manifest in Azure Container Registry
 * using a repository-scoped [ContainerRepository][com.azure.containers.containerregistry.ContainerRepository].
 *
 * The action configures the [ArtifactManifestProperties] (writeEnabled, deleteEnabled, listEnabled, readEnabled)
 * and applies them atomically to the manifest.
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when updating
 * multiple manifests in a task action.
 */
abstract class UpdateManifestPropertiesAction : WorkAction<UpdateManifestPropertiesAction.Parameters> {
    /**
     * Parameters for [UpdateManifestPropertiesAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the repository-scoped Container Repository client.
         */
        @get:Internal
        val service: Property<ContainerRepositoryClientBuildService>

        /**
         * The digest of the manifest to update, e.g., `sha256:abc123def456...`. This is a unique
         * identifier for the manifest content.
         */
        val digest: Property<String>

        /**
         * Whether the manifest can be written to (push new versions). Set to false to prevent manifest mutations.
         */
        val canWrite: Property<Boolean>

        /**
         * Whether the manifest can be deleted. Set to false to prevent manifest deletion.
         */
        val canDelete: Property<Boolean>

        /**
         * Whether the manifest can be listed. Set to false to hide the manifest from list operations.
         */
        val canList: Property<Boolean>

        /**
         * Whether the manifest can be read. Set to false to prevent manifest access.
         */
        val canRead: Property<Boolean>
    }

    override fun execute() {
        val props = ArtifactManifestProperties()
            .setWriteEnabled(parameters.canWrite.get())
            .setDeleteEnabled(parameters.canDelete.get())
            .setListEnabled(parameters.canList.get())
            .setReadEnabled(parameters.canRead.get())

        parameters.service.get().getClient()
            .getArtifact(parameters.digest.get())
            .updateManifestProperties(props)
    }
}
