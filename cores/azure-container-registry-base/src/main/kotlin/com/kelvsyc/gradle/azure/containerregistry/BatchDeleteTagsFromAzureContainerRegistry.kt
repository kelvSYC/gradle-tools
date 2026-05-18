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
 * Deletes a set of tags from an Azure Container Registry repository concurrently via [DeleteTagAction] submissions
 * to [WorkerExecutor.noIsolation]. The task is not cached because deleting from an external service is not a
 * repeatable operation.
 *
 * The task fails if any tag deletion fails.
 */
@DisableCachingByDefault(because = "Deleting from an external service is not cacheable")
abstract class BatchDeleteTagsFromAzureContainerRegistry @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /**
     * Build service managing the repository-scoped Container Repository client.
     */
    @get:ServiceReference
    abstract val service: Property<ContainerRepositoryClientBuildService>

    /**
     * The set of tag names to delete from the repository, e.g., `v1.0.0`, `latest`, or `dev-2026-05-17`.
     */
    @get:Input
    abstract val tags: SetProperty<String>

    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        tags.get().forEach { tagName ->
            queue.submit(DeleteTagAction::class.java) { params ->
                params.service.set(this@BatchDeleteTagsFromAzureContainerRegistry.service)
                params.tagName.set(tagName)
            }
        }
    }
}
