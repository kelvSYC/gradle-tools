package com.kelvsyc.gradle.azure.containerapp

import com.azure.core.management.AzureEnvironment
import com.azure.core.management.profile.AzureProfile
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing a [ContainerAppsApiManager] instance scoped to an Azure subscription,
 * resource group, and Container Apps managed environment.
 *
 * This is the root service for the `azure-container-apps-base` component. It is the credential
 * anchor — all credential configuration belongs here. Scoped services ([ContainerAppBuildService]
 * and [ContainerAppJobBuildService]) chain from this service and inherit access to the manager.
 *
 * Configure credentials at registration time using the extension functions on [AzureBuildServiceParams]
 * ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 * [managedIdentity][com.kelvsyc.gradle.azure.managedIdentity],
 * [clientSecret][com.kelvsyc.gradle.azure.clientSecret]). ARM requires a
 * [com.azure.core.credential.TokenCredential]; SAS and named-key variants are rejected with
 * [IllegalArgumentException] at initialization. When [AzureBuildServiceParams.credentialSource]
 * is unset, [com.azure.identity.DefaultAzureCredential] is used automatically.
 */
abstract class ContainerAppsEnvironmentBuildService :
    AbstractAzureClientBuildService<ContainerAppsApiManager, ContainerAppsEnvironmentBuildService.Params>() {

    /**
     * Configuration parameters for [ContainerAppsEnvironmentBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /** Azure subscription ID containing the target managed environment. */
        val subscriptionId: Property<String>

        /** Azure resource group containing the target managed environment. */
        val resourceGroupName: Property<String>

        /** Name of the Container Apps managed environment. */
        val environmentName: Property<String>
    }

    override fun createClient(): ContainerAppsApiManager {
        val tokenCredential = resolveTokenCredential() ?: DefaultAzureCredentialBuilder().build()
        val profile = AzureProfile(null, parameters.subscriptionId.get(), AzureEnvironment.AZURE)
        return ContainerAppsApiManager.authenticate(tokenCredential, profile)
    }
}
