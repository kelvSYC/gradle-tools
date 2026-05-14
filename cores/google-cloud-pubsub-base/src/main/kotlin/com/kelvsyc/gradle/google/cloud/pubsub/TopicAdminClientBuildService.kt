package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.kelvsyc.gradle.google.cloud.AbstractGcpClientBuildService
import com.kelvsyc.gradle.google.cloud.GcpBuildServiceParams

/**
 * Build service managing a [TopicAdminClient] instance.
 *
 * `TopicAdminClient` supports both topic administration and message publishing.
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent],
 * configuring the credential source via the extension functions on [GcpBuildServiceParams] (e.g.
 * [applicationDefault][com.kelvsyc.gradle.google.cloud.applicationDefault],
 * [serviceAccount][com.kelvsyc.gradle.google.cloud.serviceAccount]). The same registration can
 * then be shared with value sources, work actions and tasks via a
 * `Property<TopicAdminClientBuildService>` parameter.
 */
abstract class TopicAdminClientBuildService :
    AbstractGcpClientBuildService<TopicAdminClient, GcpBuildServiceParams>() {

    override fun createClient(): TopicAdminClient {
        val settings = TopicAdminSettings.newBuilder().apply {
            resolveCredentialsProvider()?.let { credentialsProvider = it }
        }.build()
        return TopicAdminClient.create(settings)
    }
}
