package com.kelvsyc.gradle.azure.servicebus

import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusSenderClient
import com.kelvsyc.gradle.azure.AbstractAzureClientBuildService
import com.kelvsyc.gradle.azure.AzureBuildServiceParams
import org.gradle.api.provider.Property

/**
 * Build service managing a [ServiceBusSenderClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring [Params.namespace] and exactly one of [Params.queueName] or [Params.topicName],
 * plus the credential source via the [AzureBuildServiceParams] extension functions
 * ([defaultCredential][com.kelvsyc.gradle.azure.defaultCredential],
 * [managedIdentity][com.kelvsyc.gradle.azure.managedIdentity],
 * [clientSecret][com.kelvsyc.gradle.azure.clientSecret]).
 *
 * Azure Service Bus only accepts `TokenCredential`-shaped credentials; configuring this service
 * with [sasToken][com.kelvsyc.gradle.azure.sasToken] or
 * [sharedKey][com.kelvsyc.gradle.azure.sharedKey] will fail at execution time with an
 * [IllegalArgumentException] from [resolveTokenCredential].
 *
 * Setting neither or both of [Params.queueName] and [Params.topicName] will fail at client
 * construction time.
 */
abstract class ServiceBusSenderClientBuildService :
    AbstractAzureClientBuildService<ServiceBusSenderClient, ServiceBusSenderClientBuildService.Params>() {

    /**
     * Configuration parameters for [ServiceBusSenderClientBuildService].
     */
    interface Params : AzureBuildServiceParams {
        /**
         * Fully-qualified Service Bus namespace hostname,
         * e.g. `{namespace}.servicebus.windows.net`.
         */
        val namespace: Property<String>

        /**
         * Name of the queue to send messages to. Set this XOR [topicName].
         */
        val queueName: Property<String>

        /**
         * Name of the topic to send messages to. Set this XOR [queueName].
         */
        val topicName: Property<String>
    }

    override fun createClient(): ServiceBusSenderClient {
        val outerBuilder = ServiceBusClientBuilder()
            .fullyQualifiedNamespace(parameters.namespace.get())
        resolveTokenCredential()?.let { outerBuilder.credential(it) }
        val senderBuilder = outerBuilder.sender()
        return when {
            parameters.queueName.isPresent && !parameters.topicName.isPresent ->
                senderBuilder.queueName(parameters.queueName.get())
            parameters.topicName.isPresent && !parameters.queueName.isPresent ->
                senderBuilder.topicName(parameters.topicName.get())
            else -> error(
                "Exactly one of queueName or topicName must be set on ServiceBusSenderClientBuildService"
            )
        }.buildClient()
    }
}
