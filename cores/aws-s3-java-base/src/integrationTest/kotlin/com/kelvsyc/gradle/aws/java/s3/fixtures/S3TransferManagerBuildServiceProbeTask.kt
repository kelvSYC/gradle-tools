package com.kelvsyc.gradle.aws.java.s3.fixtures

import com.kelvsyc.gradle.aws.java.s3.S3TransferManagerBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `S3TransferManagerBuildService` parameters at task-execution time by querying
 * the `service` property. The body never calls `getClient()` so the probe stays focused on the parameter
 * isolation boundary, including the nested `baseService: Property<S3AsyncClientBuildService>` reference.
 */
abstract class S3TransferManagerBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<S3TransferManagerBuildService>

    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
