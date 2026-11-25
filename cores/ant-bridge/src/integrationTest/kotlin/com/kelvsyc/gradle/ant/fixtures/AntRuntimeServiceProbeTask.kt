package com.kelvsyc.gradle.ant.fixtures

import com.kelvsyc.gradle.ant.internal.AntRuntimeService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `AntRuntimeService` at task-execution time by querying the `service` property.
 */
abstract class AntRuntimeServiceProbeTask : DefaultTask() {
    /** The BuildService under serialization probe. */
    @get:Internal
    abstract val service: Property<AntRuntimeService>

    /** Obtains the service and logs its class. */
    @TaskAction
    fun run() {
        val s = service.get()
        logger.lifecycle("service-class={}", s::class.qualifiedName)
    }
}
