package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.management.AzureEnvironment
import com.azure.core.management.profile.AzureProfile
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.resourcemanager.appconfiguration.AppConfigurationManager
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing an [AppConfigurationManager] instance scoped to an Azure subscription
 * and resource group.
 *
 * This is the ARM management-plane service for `azure-app-configuration-base`. It provides access
 * to App Configuration store lifecycle operations (create, delete) within the configured resource
 * group. Use the data-plane services ([AppConfigurationClientBuildService] /
 * [AppConfigurationAsyncClientBuildService]) for reading and writing key-values, feature flags,
 * and snapshots.
 *
 * Configure credentials at registration time using the extension functions on [AzureBuildServiceParams]
 * ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 * [managedIdentity][com.kelvsyc.gradle.azure.managedIdentity],
 * [clientSecret][com.kelvsyc.gradle.azure.clientSecret]). ARM requires a
 * [com.azure.core.credential.TokenCredential]; SAS and named-key variants are rejected with
 * [IllegalArgumentException] at initialization. When [AzureBuildServiceParams.credentialSource]
 * is unset, [com.azure.identity.DefaultAzureCredential] is used automatically.
 *
 * Register via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent].
 */
abstract class AppConfigurationManagerBuildService :
    AbstractAzureClientBuildService<AppConfigurationManager, AppConfigurationManagerBuildService.Params>() {

    /**
     * Configuration parameters for [AppConfigurationManagerBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /**
         * Azure subscription ID containing the target App Configuration stores.
         */
        val subscriptionId: Property<String>

        /**
         * Azure resource group containing the target App Configuration stores.
         */
        val resourceGroup: Property<String>
    }

    override fun createClient(): AppConfigurationManager {
        val tokenCredential = resolveTokenCredential() ?: DefaultAzureCredentialBuilder().build()
        val profile = AzureProfile(null, parameters.subscriptionId.get(), AzureEnvironment.AZURE)
        return AppConfigurationManager.authenticate(tokenCredential, profile)
    }
}
