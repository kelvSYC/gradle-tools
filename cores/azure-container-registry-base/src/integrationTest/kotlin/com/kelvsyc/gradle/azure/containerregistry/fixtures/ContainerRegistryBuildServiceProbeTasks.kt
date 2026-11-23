package com.kelvsyc.gradle.azure.containerregistry.fixtures

import com.kelvsyc.gradle.azure.containerregistry.ContainerRegistryClientBuildService
import com.kelvsyc.gradle.azure.containerregistry.ContainerRepositoryClientBuildService
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Forces Gradle to isolate the `ContainerRegistryClientBuildService` parameters at task-execution time by querying
 * the `service` property. The body never calls `getClient()` so the probe stays focused on the isolation boundary.
 */
abstract class RegistryBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<ContainerRegistryClientBuildService>

    @TaskAction
    fun run() {
        logger.lifecycle("service-class={}", service.get()::class.qualifiedName)
    }
}

/**
 * Forces Gradle to isolate the `ContainerRepositoryClientBuildService` parameters, including its service reference to
 * the registry service, at task-execution time. The body never calls `getClient()`.
 */
abstract class RepositoryBuildServiceProbeTask : DefaultTask() {
    /** The BuildService whose parameters are under serialization probe. */
    @get:Internal
    abstract val service: Property<ContainerRepositoryClientBuildService>

    @TaskAction
    fun run() {
        logger.lifecycle("service-class={}", service.get()::class.qualifiedName)
    }
}
