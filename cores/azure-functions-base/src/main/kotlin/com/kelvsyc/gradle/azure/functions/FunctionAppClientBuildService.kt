package com.kelvsyc.gradle.azure.functions

import com.azure.core.management.AzureEnvironment
import com.azure.core.management.profile.AzureProfile
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.resourcemanager.appservice.AppServiceManager
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing an [AppServiceManager] instance scoped to an Azure subscription and
 * resource group.
 *
 * This is the root ARM service for the azure-functions-base component. It provides access to all
 * function apps within the configured resource group. Individual [com.azure.resourcemanager.appservice.models.FunctionApp]
 * instances are resolved from the manager at the point of use — ValueSources and WorkActions
 * supply [appName][com.kelvsyc.gradle.azure.functions.FunctionAppClientBuildService.Params.resourceGroup]
 * as a local param rather than baking the app name into the service registration.
 *
 * Configure credentials at registration time using the extension functions on [AzureBuildServiceParams]
 * ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 * [managedIdentity][com.kelvsyc.gradle.azure.managedIdentity],
 * [clientSecret][com.kelvsyc.gradle.azure.clientSecret]). ARM requires a
 * [com.azure.core.credential.TokenCredential]; SAS and named-key variants are rejected with
 * [IllegalArgumentException] at initialization. When [AzureBuildServiceParams.credentialSource]
 * is unset, [com.azure.identity.DefaultAzureCredential] is used automatically.
 */
abstract class FunctionAppClientBuildService :
    AbstractAzureClientBuildService<AppServiceManager, FunctionAppClientBuildService.Params>() {

    /**
     * Configuration parameters for [FunctionAppClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /** Azure subscription ID containing the target function apps. */
        val subscriptionId: Property<String>

        /** Azure resource group containing the target function apps. */
        val resourceGroup: Property<String>
    }

    override fun createClient(): AppServiceManager {
        val tokenCredential = resolveTokenCredential() ?: DefaultAzureCredentialBuilder().build()
        val profile = AzureProfile(null, parameters.subscriptionId.get(), AzureEnvironment.AZURE)
        return AppServiceManager.authenticate(tokenCredential, profile)
    }
}
