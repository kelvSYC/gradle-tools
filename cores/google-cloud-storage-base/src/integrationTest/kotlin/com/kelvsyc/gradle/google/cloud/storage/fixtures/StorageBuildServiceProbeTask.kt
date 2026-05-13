package com.kelvsyc.gradle.google.cloud.storage.fixtures

import com.kelvsyc.gradle.google.cloud.storage.StorageClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `StorageClientBuildService` parameters at task-execution time by querying
 * the `service` property. The body never calls `getClient()` so the probe stays focused on the isolation
 * boundary.
 */
abstract class StorageBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<StorageClientBuildService>

    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
