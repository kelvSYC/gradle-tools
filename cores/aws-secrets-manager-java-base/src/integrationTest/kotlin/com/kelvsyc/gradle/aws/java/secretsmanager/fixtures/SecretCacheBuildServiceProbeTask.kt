package com.kelvsyc.gradle.aws.java.secretsmanager.fixtures

import com.kelvsyc.gradle.aws.java.secretsmanager.SecretCacheBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `SecretCacheBuildService` parameters at task-execution time by querying the
 * `service` property. The body never calls `getClient()` so the probe focuses on the parameter isolation
 * boundary, including the nested `baseService: Property<SecretsManagerClientBuildService>` reference.
 */
abstract class SecretCacheBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<SecretCacheBuildService>

    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
