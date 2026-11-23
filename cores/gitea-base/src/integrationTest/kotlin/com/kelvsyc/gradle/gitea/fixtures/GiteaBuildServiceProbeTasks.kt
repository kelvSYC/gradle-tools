package com.kelvsyc.gradle.gitea.fixtures

import com.kelvsyc.gradle.gitea.GiteaBasicClientBuildService
import com.kelvsyc.gradle.gitea.GiteaBearerClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `GiteaBasicClientBuildService` parameters at task-execution time by querying the
 * `service` property. The body never calls `getClient()` so the probe stays focused on the isolation boundary.
 */
abstract class GiteaBasicBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<GiteaBasicClientBuildService>

    @TaskAction
    fun run() {
        logger.lifecycle("service-class={}", service.get()::class.qualifiedName)
    }
}

/**
 * Forces Gradle to isolate the `GiteaBearerClientBuildService` parameters at task-execution time by querying the
 * `service` property. The body never calls `getClient()` so the probe stays focused on the isolation boundary.
 */
abstract class GiteaBearerBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<GiteaBearerClientBuildService>

    @TaskAction
    fun run() {
        logger.lifecycle("service-class={}", service.get()::class.qualifiedName)
    }
}
