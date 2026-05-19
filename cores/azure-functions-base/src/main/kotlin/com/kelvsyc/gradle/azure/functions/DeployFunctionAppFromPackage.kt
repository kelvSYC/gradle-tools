package com.kelvsyc.gradle.azure.functions

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task that sets `WEBSITE_RUN_FROM_PACKAGE` to a plain blob URL on an Azure Function App.
 *
 * Adds `@ServiceReference` tracking to [appService] and exposes [packageUrl] as `@Input` for
 * basic up-to-date checking on URL changes.
 *
 * For SAS URL deployments, subclass [AbstractDeployFunctionAppFromPackage] and override
 * [configureAction] to call [RunFromPackageFunctionAppAction.Parameters.sasUrl].
 */
@DisableCachingByDefault(because = "Deploying to an external service is not cacheable")
abstract class DeployFunctionAppFromPackage @Inject constructor(
    workerExecutor: WorkerExecutor,
) : AbstractDeployFunctionAppFromPackage(workerExecutor) {

    @get:ServiceReference
    abstract override val appService: Property<FunctionAppClientBuildService>

    /**
     * The name of the function app to update.
     */
    @get:Input
    abstract override val appName: Property<String>

    /**
     * The plain blob URL to set as `WEBSITE_RUN_FROM_PACKAGE`.
     */
    @get:Input
    abstract val packageUrl: Property<String>

    override fun configureAction(params: RunFromPackageFunctionAppAction.Parameters) {
        params.plainUrl(packageUrl.get())
    }
}
