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

        val functionName = parameters.functionName.get()
        val envelope = functionApp.listFunctions()
            .firstOrNull { func ->
                func.innerModel().id().orEmpty().substringAfterLast('/') == functionName
            }
            ?: throw IllegalArgumentException("Function '$functionName' not found in app '${parameters.appName.get()}'")

        val invokeUrlTemplate = envelope.innerModel().invokeUrlTemplate().orEmpty()
        // ARM SDK 2.x does not expose authLevel on FunctionEnvelope; default to the most restrictive safe value.
        return FunctionInfo(
            name = functionName,
            invokeUrlTemplate = invokeUrlTemplate,
            authLevel = FunctionAuthLevel.FUNCTION,
        )
    }
}
