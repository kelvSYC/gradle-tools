package com.kelvsyc.gradle.azure.functions

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Base task that sets `WEBSITE_RUN_FROM_PACKAGE` on an Azure Function App, causing Azure to
 * mount the specified blob as the function package on next cold start.
 *
 * Internally delegates to [RunFromPackageFunctionAppAction]. Up-to-date checking is intentionally
 * left to concrete subclasses:
 * - Plain blob URL: the blob may be updated out-of-band; Gradle cannot determine freshness from the URL alone.
 * - SAS URL: structurally different on each generation.
 *
 * Subclass and add `@Input` or `@InputFile` properties that reflect what should trigger
 * re-deployment in your context. [DeployFunctionAppFromPackage] is a no-extra-inputs concrete
 * class for always-run scenarios.
 */
@DisableCachingByDefault(because = "Deploying to an external service is not cacheable")
abstract class AbstractDeployFunctionAppFromPackage @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The ARM manager service.
     */
    @get:Internal
    abstract val appService: Property<FunctionAppClientBuildService>

    /**
     * The name of the function app to update.
     */
    @get:Internal
    abstract val appName: Property<String>

    /**
     * Called during [run] to configure the URL on the [RunFromPackageFunctionAppAction.Parameters].
     * Subclasses call [RunFromPackageFunctionAppAction.Parameters.plainUrl] or
     * [RunFromPackageFunctionAppAction.Parameters.sasUrl] here.
     */
    protected abstract fun configureAction(params: RunFromPackageFunctionAppAction.Parameters)

    /**
     * Submits a [RunFromPackageFunctionAppAction] to update `WEBSITE_RUN_FROM_PACKAGE`.
     */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(RunFromPackageFunctionAppAction::class.java) { params ->
            params.appService.set(appService)
            params.appName.set(appName)
            configureAction(params)
        }
    }
}
