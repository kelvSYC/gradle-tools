package com.kelvsyc.gradle.aws.java.imds.fixtures

import com.kelvsyc.gradle.aws.java.imds.ImdsClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `ImdsClientBuildService` parameters at task-execution time by querying the
 * `service` property. The body never calls `getClient()` so the probe stays focused on the isolation
 * boundary — in particular whether `Params.endpointMode: Property<EndpointMode>` (an AWS SDK enum) survives
 * the config-cache round-trip.
 */
abstract class ImdsClientBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<ImdsClientBuildService>

    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
