package com.kelvsyc.gradle.bitbucket.cloud.fixtures

import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `BitbucketCloudClientBuildService` parameters at task-execution time by
 * querying the `service` property. The body never calls `getClient()` so the probe stays focused on the
 * isolation boundary — in particular whether `Params.credentials: Property<PasswordCredentials>` survives
 * the config-cache round-trip.
 */
abstract class BitbucketCloudClientBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<BitbucketCloudClientBuildService>

    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
