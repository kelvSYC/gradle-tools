package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Specialization of [AbstractWaitForJobExecutionTask] that adds `@get:ServiceReference` to the
 * [service] property so Gradle automatically tracks the build service as a task dependency.
 *
 * Register this task directly when you need to wait for an execution that was previously
 * submitted by [RunJobTask]:
 *
 * ```kotlin
 * val cloudRunExecutions = gradle.sharedServices.registerIfAbsent("cloudRunExecutions", CloudRunExecutionsClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 *
 * tasks.register<WaitForJobExecutionTask>("waitForExecution") {
 *     service.set(cloudRunExecutions)
 *     executionNameFile.set(file("build/execution-name.txt"))
 * }
 * ```
 */
abstract class WaitForJobExecutionTask @Inject constructor(
    workerExecutor: WorkerExecutor,
) : AbstractWaitForJobExecutionTask(workerExecutor) {

    @get:ServiceReference
    abstract override val service: Property<CloudRunExecutionsClientBuildService>
}
