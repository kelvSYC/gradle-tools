package com.kelvsyc.gradle.aws.java.secretsmanager.fixtures

import com.kelvsyc.gradle.aws.java.secretsmanager.SecretsManagerClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `SecretsManagerClientBuildService` parameters at task-execution time by
 * querying the `service` property. The body never calls `getClient()` so that the probe stays focused on the
 * isolation boundary, not on AWS SDK runtime concerns like default region resolution.
 */
abstract class SecretsManagerClientBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<SecretsManagerClientBuildService>

    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
