package com.kelvsyc.gradle.aws.java.sns.fixtures

import com.kelvsyc.gradle.aws.java.sns.SnsClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import javax.inject.Inject

/**
 * Dispatches a [BuildServiceWorkAction] via `WorkerExecutor.noIsolation()` — the Variant A baseline probe.
 */
abstract class BuildServiceWorkerProbeTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /** The BuildService to pass on the `WorkParameters`. */
    @get:Internal
    abstract val service: Property<SnsClientBuildService>

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
 * Dispatches a [ByoClientWorkAction] via `WorkerExecutor.noIsolation()`, materializing a live `SnsClient`
 * inside the task action and setting it on the `WorkParameters`. This is the Variant B probe that answers
 * whether `Property<LiveClient>` survives `WorkerExecutor` submission serialization.
 */
abstract class ByoClientWorkerProbeTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /** The AWS region used to construct the probe client. */
    @get:Internal
    abstract val region: Property<String>

    @TaskAction
    fun run() {
        val client: SnsClient = SnsClient.builder()
            .region(Region.of(region.get()))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .build()
        val queue = workerExecutor.noIsolation()
        queue.submit(ByoClientWorkAction::class.java) { params ->
            params.client.set(client)
        }
        queue.await()
    }
}
