package com.kelvsyc.gradle.aws.java.s3.fixtures

import com.kelvsyc.gradle.aws.java.s3.S3ClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `S3ClientBuildService` parameters at task-execution time by querying the
 * `service` property. The body never calls `getClient()` so that the probe stays focused on the isolation
 * boundary, not on AWS SDK runtime concerns like default region resolution.
 */
abstract class S3ClientBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<S3ClientBuildService>

    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
