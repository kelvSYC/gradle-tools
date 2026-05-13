package com.kelvsyc.gradle.google.cloud.storage.fixtures

import com.google.cloud.NoCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.kelvsyc.gradle.google.cloud.storage.StorageClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Dispatches a [BuildServiceWorkAction] via `WorkerExecutor.noIsolation()` — the Variant A baseline probe.
 */
abstract class BuildServiceWorkerProbeTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /** The BuildService to pass on the `WorkParameters`. */
    @get:Internal
    abstract val service: Property<StorageClientBuildService>

    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        val s = service
        queue.submit(BuildServiceWorkAction::class.java) { params ->
            params.service.set(s)
        }
        queue.await()
    }
}

/**
 * Dispatches a [ByoClientWorkAction] via `WorkerExecutor.noIsolation()`, materializing a live `Storage`
 * client inside the task action and setting it on the `WorkParameters` — the Variant B probe.
 */
abstract class ByoClientWorkerProbeTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /** The GCP project id used to construct the probe client. */
    @get:Internal
    abstract val projectId: Property<String>

    @TaskAction
    fun run() {
        val client: Storage = StorageOptions.newBuilder()
            .setProjectId(projectId.get())
            .setCredentials(NoCredentials.getInstance())
            .build()
            .service
        val queue = workerExecutor.noIsolation()
        queue.submit(ByoClientWorkAction::class.java) { params ->
            params.client.set(client)
        }
        queue.await()
    }
}
