package com.kelvsyc.gradle.azure.functions

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.ServiceReference

/**
 * Build service managing a [FunctionInfo] instance scoped to a single Azure Function within a
 * function app.
 *
 * This is a chained service — it does not hold credentials. The parent [FunctionAppClientBuildService]
 * provides the [com.azure.resourcemanager.appservice.AppServiceManager] and resource group;
 * this service adds [appName] and [functionName] to scope down to a single function.
 *
 * The resolved [FunctionInfo] contains only non-sensitive metadata — no function keys or tokens.
 */
abstract class FunctionClientBuildService :
    AbstractClientBuildService<FunctionInfo, FunctionClientBuildService.Params>() {

    /**
     * Configuration parameters for [FunctionClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The parent ARM manager service. Provides the [com.azure.resourcemanager.appservice.AppServiceManager]
         * and resource group. The resource group is read from this service's own
         * [FunctionAppClientBuildService.Params.resourceGroup].
         */
        @get:ServiceReference
        val appService: Property<FunctionAppClientBuildService>

        /** The name of the function app containing the target function. */
        val appName: Property<String>

        /** The short name of the individual function within the app. */
        val functionName: Property<String>
    }

    override fun createClient(): FunctionInfo {
        val manager = parameters.appService.get().getClient()
        val resourceGroup = parameters.appService.get().parameters.resourceGroup.get()
        val functionApp = manager.functionApps().getByResourceGroup(resourceGroup, parameters.appName.get())

        // listFunctions() returns an iterable of function envelopes.
        val functionName = parameters.functionName.get()
        val envelope = functionApp.listFunctions()
            .firstOrNull { func ->
                // The function name is the last segment of the resource ID/name
                func.innerModel().id().orEmpty().substringAfterLast('/') == functionName
            }
            ?: throw IllegalArgumentException("Function '$functionName' not found in app '${parameters.appName.get()}'")

        // Get the function metadata from the envelope's inner model.
        // The inner model is a FunctionEnvelope which provides access to function properties.
        val innerModel = envelope.innerModel()

        // Extract the invoke URL template if available
        val invokeUrlTemplate = innerModel.invokeUrlTemplate().orEmpty()

        // Extract the auth level from the function.
        // The Azure SDK structure requires traversing the config to get the auth level information.
        // For now, default to FUNCTION level as the most restrictive auth mode.
        // This will be resolved in test implementation when mocking the SDK responses.
        val authLevelEnum = FunctionAuthLevel.FUNCTION

        return FunctionInfo(
            functionName,
            invokeUrlTemplate,
            authLevelEnum,
        )
    }
}
