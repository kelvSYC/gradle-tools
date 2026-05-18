package com.kelvsyc.gradle.azure.containerregistry

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Deletes a set of manifests from an Azure Container Registry repository concurrently via [DeleteManifestAction]
 * submissions to [WorkerExecutor.noIsolation]. The task is not cached because deleting from an external service
 * is not a repeatable operation.
 *
 * Deleting a manifest also deletes all tags that reference it. The task fails if any manifest deletion fails.
 */
@DisableCachingByDefault(because = "Deleting from an external service is not cacheable")
abstract class BatchDeleteManifestsFromAzureContainerRegistry @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /**
     * Build service managing the repository-scoped Container Repository client.
     */
    @get:ServiceReference
    abstract val service: Property<ContainerRepositoryClientBuildService>

    /**
     * The set of manifest digests to delete from the repository, e.g., `sha256:abc123def456...` or
     * `sha256:0123456789abcdef...`. Each digest is a unique identifier for a manifest's content.
     */
    @get:Input
    abstract val digests: SetProperty<String>

    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        digests.get().forEach { digest ->
            queue.submit(DeleteManifestAction::class.java) { params ->
                params.service.set(this@BatchDeleteManifestsFromAzureContainerRegistry.service)
                params.digest.set(digest)
            }
        }
    }
}
