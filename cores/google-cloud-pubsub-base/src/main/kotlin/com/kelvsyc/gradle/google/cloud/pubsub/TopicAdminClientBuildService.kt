package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [TopicAdminClient] instance.
 *
 * `TopicAdminClient` supports both topic administration and message publishing.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], configuring
 * [Params.credentials] as needed. The same registration can then be shared with value sources, work
 * actions and tasks via a `Property<TopicAdminClientBuildService>` parameter.
 */
abstract class TopicAdminClientBuildService :
    AbstractClientBuildService<TopicAdminClient, TopicAdminClientBuildService.Params>() {
    /**
     * Configuration parameters for [TopicAdminClientBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The Google API [CredentialsProvider] used for authentication.
         *
         * If unset, the underlying client uses application default credentials.
         */
        val credentials: Property<CredentialsProvider>
    }

    override fun createClient(): TopicAdminClient {
        val settings = TopicAdminSettings.newBuilder().apply {
            if (parameters.credentials.isPresent) {
                credentialsProvider = parameters.credentials.get()
            }
        }.build()
        return TopicAdminClient.create(settings)
    }
}
