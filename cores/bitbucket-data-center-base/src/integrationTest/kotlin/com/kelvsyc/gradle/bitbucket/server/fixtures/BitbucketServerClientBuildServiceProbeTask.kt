package com.kelvsyc.gradle.bitbucket.server.fixtures

import com.kelvsyc.gradle.bitbucket.server.BitbucketServerClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `BitbucketServerClientBuildService` parameters at task-execution time by
 * querying the `service` property. The body never calls `getClient()` so the probe stays focused on the
 * isolation boundary. All params are `Property<String>` so this should always succeed; the probe serves as
 * a baseline regression sentinel.
 */
abstract class BitbucketServerClientBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<BitbucketServerClientBuildService>

    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
