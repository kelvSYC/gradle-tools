package com.kelvsyc.gradle.azure.servicebus

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing a [ServiceBusAdministrationClient] instance.
 *
 * Used for administrative operations: listing queues, topics, and subscriptions within a namespace.
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [Params.namespace] and the credential source via the [AzureBuildServiceParams]
 * extension functions
 * ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 * [managedIdentity][com.kelvsyc.gradle.azure.managedIdentity],
 * [clientSecret][com.kelvsyc.gradle.azure.clientSecret]).
 *
 * Azure Service Bus only accepts `TokenCredential`-shaped credentials; configuring this service
 * with [sasToken][com.kelvsyc.gradle.azure.sasToken] or
 * [sharedKey][com.kelvsyc.gradle.azure.sharedKey] will fail at execution time with an
 * [IllegalArgumentException] from [resolveTokenCredential].
 */
abstract class ServiceBusAdministrationClientBuildService :
    AbstractAzureClientBuildService<ServiceBusAdministrationClient, ServiceBusAdministrationClientBuildService.Params>() {

    /**
     * Configuration parameters for [ServiceBusAdministrationClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /**
         * Fully-qualified Service Bus namespace hostname,
         * e.g. `{namespace}.servicebus.windows.net`.
         */
        val namespace: Property<String>
    }

    override fun createClient(): ServiceBusAdministrationClient {
        val builder = ServiceBusAdministrationClientBuilder()
            .endpoint("https://${parameters.namespace.get()}")
        resolveTokenCredential()?.let { builder.credential(it) }
        return builder.buildClient()
    }
}
