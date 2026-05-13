package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.Credentials
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [SecretManagerServiceClient] instance.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.credentials] as needed. The same registration can then be shared with value sources and work
 * actions via a `Property<SecretManagerServiceClientBuildService>` parameter.
 */
abstract class SecretManagerServiceClientBuildService :
    AbstractClientBuildService<SecretManagerServiceClient, SecretManagerServiceClientBuildService.Params>() {
    /**
     * Configuration parameters for [SecretManagerServiceClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The credentials used to access Google Cloud Secret Manager.
         *
         * If unset, the underlying client uses application default credentials.
         */
        val credentials: Property<Credentials>
    }

    override fun createClient(): SecretManagerServiceClient {
        val settings = SecretManagerServiceSettings.newBuilder().apply {
            if (parameters.credentials.isPresent) {
                credentialsProvider = FixedCredentialsProvider.create(parameters.credentials.get())
            }
        }.build()
        return SecretManagerServiceClient.create(settings)
    }
}
