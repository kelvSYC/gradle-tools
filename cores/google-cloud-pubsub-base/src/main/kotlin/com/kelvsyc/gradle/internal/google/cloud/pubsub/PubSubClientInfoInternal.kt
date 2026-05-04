package com.kelvsyc.gradle.internal.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.kelvsyc.gradle.google.cloud.pubsub.PubSubClientInfo

abstract class PubSubClientInfoInternal : PubSubClientInfo, ServiceClientInfoInternal<TopicAdminClient> {
    override fun createClient(): TopicAdminClient {
        val settings = TopicAdminSettings.newBuilder().apply {
            credentialsProvider = credentials.get()
        }.build()

        return TopicAdminClient.create(settings)
    }
}
